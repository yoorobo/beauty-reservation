package com.example.springapp.service;

import com.example.springapp.domain.BeautyService;
import com.example.springapp.dto.BeautyServiceForm;
import com.example.springapp.repository.BeautyServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeautyServiceService {

    private final BeautyServiceRepository beautyServiceRepository;

    // 목록 [SR-04]
    public List<BeautyService> findAll() {
        return beautyServiceRepository.findAll();
    }

    // 단건 조회
    public BeautyService findById(Long id) {
        return beautyServiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("시술을 찾을 수 없습니다. id=" + id));
    }

    // 등록 [SR-11]
    @Transactional
    public Long create(BeautyServiceForm form) {
        BeautyService service = new BeautyService();
        service.setServiceName(form.getServiceName());
        service.setPrice(form.getPrice());
        service.setDuration(form.getDuration());
        service.setDescription(form.getDescription());
        return beautyServiceRepository.save(service).getId();
    }

    // 수정 [SR-11]
    @Transactional
    public void update(Long id, BeautyServiceForm form) {
        BeautyService service = findById(id);
        service.setServiceName(form.getServiceName());
        service.setPrice(form.getPrice());
        service.setDuration(form.getDuration());
        service.setDescription(form.getDescription());
    }

    // 삭제 [SR-11]
    @Transactional
    public void delete(Long id) {
        // TODO SR-11: 예약 참조 삭제거부 D7에서 추가 (예약 엔티티는 D5 생성)
        beautyServiceRepository.deleteById(id);
    }
}
