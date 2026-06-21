package com.clubmanager.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
@Entity
public class ClubAnalysis extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private LocalDate analysisDate;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubAnalysisItem> items = new ArrayList<>();

    public void addItem(ClubAnalysisItem item) {
        item.setAnalysis(this);
        items.add(item);
    }
}
