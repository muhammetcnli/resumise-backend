package com.resumise.backend.mapper;

import com.resumise.backend.dto.CvGetResponse;
import com.resumise.backend.model.Cv;
import org.springframework.stereotype.Component;

@Component
public class CvMapper {

    public CvGetResponse toGetResponse(Cv cv) {
        return new CvGetResponse(
                cv.getId(),
                cv.getTitle(),
                cv.getFileName(),
                cv.getFileType(),
                cv.getFileSize(),
                cv.isDefault()
        );
    }
}

