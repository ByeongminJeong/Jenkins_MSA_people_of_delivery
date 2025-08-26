package com.example.cloudfour.storeservice.domain.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegionResponseDTO {
    String siDo;
    String siGunGu;
    String eupMyeonDong;
}