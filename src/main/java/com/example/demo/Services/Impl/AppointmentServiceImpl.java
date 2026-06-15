package com.example.demo.Services.Impl;

import com.example.demo.DTOs.BookAppointmentRequest;
import com.example.demo.DTOs.AppointmentResponse;
import com.example.demo.Entities.Appointment;
import com.example.demo.Entities.Schedule;
import com.example.demo.Entities.AppointmentStatus;
import com.example.demo.Repositories.AppointmentRepository;
import com.example.demo.Repositories.ScheduleRepository;
import com.example.demo.Services.AppointmentService;
import com.example.demo.Services.ScheduleService;
import com.example.demo.Services.EmailService;
import com.example.demo.Services.QrCodeService;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.Exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final EmailService emailService;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    // Điểm nhấn CV: Toàn bộ quá trình đặt lịch, trừ chỗ và tạo QR nằm chung một Transaction bảo toàn toàn vẹn dữ liệu
    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        log.info("Bệnh nhân ID {} đang thực hiện đặt lịch khám...", request.getPatientId());

        // 1. Kiểm tra trùng lịch: Bệnh nhân không được phép đặt 2 lịch hẹn trong cùng một khung giờ của một ngày
        Schedule requestedSchedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch khám phù hợp."));

        boolean isOverlapped = appointmentRepository.existsByPatientIdAndScheduleDateAndScheduleStartTime(
                request.getPatientId(), requestedSchedule.getDate(), requestedSchedule.getStartTime());

        if (isOverlapped) {
            throw new BadRequestException("Bạn đã có một lịch hẹn khác vào khung giờ này trong ngày.");
        }

        // 2. Thực hiện trừ chỗ trống (Gọi qua ScheduleService để tận dụng Optimistic Locking xử lý Concurrency)
        scheduleService.reserveSlot(request.getScheduleId());

        // 3. Khởi tạo đối tượng lịch hẹn mới
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setSchedule(requestedSchedule);
        appointment.setSymptoms(request.getSymptoms());
        appointment.setStatus(AppointmentStatus.PENDING); // Trạng thái mặc định ban đầu là Chờ duyệt

        // Lưu trước để lấy ID tự tăng, phục vụ việc mã hóa QR Code
        appointment = appointmentRepository.save(appointment);

        // 4. Sinh mã QR Check-in tự động dựa trên ID lịch hẹn
        try {
            String qrCodePath = qrCodeService.generateAppointmentQrCode(appointment.getId());
            appointment.setQrCode(qrCodePath);
            appointment = appointmentRepository.save(appointment); // Cập nhật lại mã QR vào DB
        } catch (Exception e) {
            log.error("Lỗi khi sinh mã QR Code cho lịch hẹn ID: {}", appointment.getId(), e);
            // Có thể bỏ qua hoặc xử lý tạo lại sau, tránh làm roll-back luồng đặt lịch chính của khách
        }

        // 5. Gửi email thông báo đặt lịch thành công một cách bất đồng bộ (Async)
        try {
            emailService.sendBookingEmail(appointment.getPatientId(), appointment);
        } catch (Exception e) {
            log.error("Lỗi gửi email xác nhận đặt lịch cho bệnh nhân: {}", request.getPatientId(), e);
        }

        return convertToResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, AppointmentStatus status) {
        log.info("Cập nhật trạng thái lịch hẹn ID {} thành: {}", appointmentId, status);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin lịch hẹn này."));

        appointment.setStatus(status);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return convertToResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, String reason) {
        log.warn("Bệnh nhân/Hệ thống yêu cầu hủy lịch hẹn ID: {}, lý do: {}", appointmentId, reason);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin lịch hẹn cần hủy."));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Không thể hủy lịch hẹn đã hoàn thành hoặc đã bị hủy trước đó.");
        }

        // 1. Chuyển trạng thái sang CANCELLED
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(reason);
        Appointment cancelledAppointment = appointmentRepository.save(appointment);

        // 2. Giải phóng slot khám của bác sĩ (Tăng lại availableSlots thêm 1 chỗ trống)
        scheduleService.releaseSlot(appointment.getSchedule().getId());

        // 3. Gửi email thông báo hủy lịch
        try {
            emailService.sendCancellationEmail(appointment.getPatientId(), appointment, reason);
        } catch (Exception e) {
            log.error("Lỗi gửi email thông báo hủy lịch.", e);
        }

        return convertToResponse(cancelledAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByScheduleDateDesc(patientId);
        return appointments.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByScheduleDoctorId(doctorId);
        return appointments.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // Hàm tiện ích convert Entity -> Response DTO
    private AppointmentResponse convertToResponse(Appointment appointment) {
        AppointmentResponse res = new AppointmentResponse();
        res.setId(appointment.getId());
        res.setPatientId(appointment.getPatientId());
        res.setDoctorId(appointment.getSchedule().getDoctor().getId());
        res.setDoctorName(appointment.getSchedule().getDoctor().getName());
        res.setDate(appointment.getSchedule().getDate());
        res.setStartTime(appointment.getSchedule().getStartTime());
        res.setEndTime(appointment.getSchedule().getEndTime());
        res.setStatus(appointment.getStatus());
        res.setSymptoms(appointment.getSymptoms());
        res.setQrCode(appointment.getQrCode());
        res.setCancelReason(appointment.getCancelReason());
        return res;
    }
}