package com.example.springapp.service;

import com.example.springapp.domain.Designer;
import com.example.springapp.dto.DesignerForm;
import com.example.springapp.repository.DesignerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignerService {

    private final DesignerRepository designerRepository;

    // 목록 [SR-05]
    public List<Designer> findAll() {
        return designerRepository.findAll();
    }

    // 단건 조회
    public Designer findById(Long id) {
        return designerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("디자이너를 찾을 수 없습니다. id=" + id));
    }

    // 등록 [SR-11]
    @Transactional
    public Long create(DesignerForm form) {
        Designer designer = new Designer();
        designer.setName(form.getName());
        designer.setSpecialty(form.getSpecialty());
        designer.setIntroduction(form.getIntroduction());
        return designerRepository.save(designer).getId();
    }

    // 수정 [SR-11]
    @Transactional
    public void update(Long id, DesignerForm form) {
        Designer designer = findById(id);
        designer.setName(form.getName());
        designer.setSpecialty(form.getSpecialty());
        designer.setIntroduction(form.getIntroduction());
    }

    // 삭제 [SR-11]
    @Transactional
    public void delete(Long id) {
        // TODO SR-11: 예약 참조 삭제거부 D7에서 추가 (예약 엔티티는 D5 생성)
        designerRepository.deleteById(id);
    }
}
