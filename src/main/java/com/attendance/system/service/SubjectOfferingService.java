package com.attendance.system.service;

import com.attendance.system.entity.AcademicSession;
import com.attendance.system.entity.Department;
import com.attendance.system.entity.Program;
import com.attendance.system.entity.RecordStatus;
import com.attendance.system.entity.Section;
import com.attendance.system.entity.Subject;
import com.attendance.system.entity.SubjectOffering;
import com.attendance.system.repository.AcademicSessionRepository;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.ProgramRepository;
import com.attendance.system.repository.SectionRepository;
import com.attendance.system.repository.SubjectOfferingRepository;
import com.attendance.system.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectOfferingService {

    private final SubjectOfferingRepository subjectOfferingRepository;
    private final SubjectRepository subjectRepository;
    private final ProgramRepository programRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final SectionRepository sectionRepository;
    private final DepartmentRepository departmentRepository;

    public SubjectOfferingService(
            SubjectOfferingRepository subjectOfferingRepository,
            SubjectRepository subjectRepository,
            ProgramRepository programRepository,
            AcademicSessionRepository academicSessionRepository,
            SectionRepository sectionRepository,
            DepartmentRepository departmentRepository
    ) {
        this.subjectOfferingRepository = subjectOfferingRepository;
        this.subjectRepository = subjectRepository;
        this.programRepository = programRepository;
        this.academicSessionRepository = academicSessionRepository;
        this.sectionRepository = sectionRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<SubjectOffering> getAllOfferings(Long collegeId) {

        return subjectOfferingRepository.findAll()
                .stream()
                .filter(offering ->
                        isOfferingInCollege(offering, collegeId)
                )
                .toList();
    }

    public Optional<SubjectOffering> getOfferingById(
            Long id,
            Long collegeId
    ) {

        return subjectOfferingRepository.findById(id)
                .filter(offering ->
                        isOfferingInCollege(offering, collegeId)
                );
    }

    public List<SubjectOffering> getOfferingsByContext(
            Long programId,
            Long academicSessionId,
            Short semesterId,
            Long sectionId,
            Long collegeId
    ) {

        if (!isProgramInCollege(programId, collegeId)
                || !isAcademicSessionInCollege(
                academicSessionId,
                collegeId
        )
                || !isSectionInCollege(sectionId, collegeId)) {

            return List.of();
        }

        return subjectOfferingRepository
                .findByProgramIdAndAcademicSessionIdAndSemesterIdAndSectionId(
                        programId,
                        academicSessionId,
                        semesterId,
                        sectionId
                );
    }

    public SubjectOffering createOffering(
            SubjectOffering offering,
            Long collegeId
    ) {

        validateOfferingContext(offering, collegeId);

        if (offering.getStatus() == null) {
            offering.setStatus(RecordStatus.active);
        }

        return subjectOfferingRepository.save(offering);
    }

    public Optional<SubjectOffering> updateOffering(
            Long id,
            SubjectOffering updatedOffering,
            Long collegeId
    ) {

        return subjectOfferingRepository.findById(id)
                .filter(existingOffering ->
                        isOfferingInCollege(
                                existingOffering,
                                collegeId
                        )
                )
                .map(existingOffering -> {

                    validateOfferingContext(
                            updatedOffering,
                            collegeId
                    );

                    existingOffering.setSubjectId(
                            updatedOffering.getSubjectId()
                    );

                    existingOffering.setProgramId(
                            updatedOffering.getProgramId()
                    );

                    existingOffering.setAcademicSessionId(
                            updatedOffering.getAcademicSessionId()
                    );

                    existingOffering.setSemesterId(
                            updatedOffering.getSemesterId()
                    );

                    existingOffering.setSectionId(
                            updatedOffering.getSectionId()
                    );

                    if (updatedOffering.getStatus() != null) {
                        existingOffering.setStatus(
                                updatedOffering.getStatus()
                        );
                    }

                    return subjectOfferingRepository.save(
                            existingOffering
                    );
                });
    }

    public boolean deleteOffering(
            Long id,
            Long collegeId
    ) {

        Optional<SubjectOffering> offering =
                subjectOfferingRepository.findById(id)
                        .filter(existingOffering ->
                                isOfferingInCollege(
                                        existingOffering,
                                        collegeId
                                )
                        );

        if (offering.isEmpty()) {
            return false;
        }

        subjectOfferingRepository.delete(offering.get());

        return true;
    }

    private void validateOfferingContext(
            SubjectOffering offering,
            Long collegeId
    ) {

        Program program = programRepository
                .findById(offering.getProgramId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Program not found"
                        )
                );

        if (!isProgramInCollege(
                program.getId(),
                collegeId
        )) {
            throw new IllegalArgumentException(
                    "Program does not belong to your college"
            );
        }

        AcademicSession academicSession =
                academicSessionRepository
                        .findById(
                                offering.getAcademicSessionId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Academic session not found"
                                )
                        );

        if (!collegeId.equals(
                academicSession.getCollegeId()
        )) {
            throw new IllegalArgumentException(
                    "Academic session does not belong to your college"
            );
        }

        Section section = sectionRepository
                .findById(offering.getSectionId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Section not found"
                        )
                );

        if (!sectionBelongsToCollege(
                section,
                collegeId
        )) {
            throw new IllegalArgumentException(
                    "Section does not belong to your college"
            );
        }

        if (!program.getId().equals(
                section.getProgramId()
        )
                || !academicSession.getId().equals(
                section.getAcademicSessionId()
        )
                || !offering.getSemesterId().equals(
                section.getSemesterId()
        )) {

            throw new IllegalArgumentException(
                    "Subject offering context must match its section"
            );
        }

        Subject subject = subjectRepository
                .findById(offering.getSubjectId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subject not found"
                        )
                );

        if (!subjectBelongsToProgram(
                subject,
                program
        )) {
            throw new IllegalArgumentException(
                    "Subject must belong to the department of the selected program"
            );
        }
    }

    private boolean isOfferingInCollege(
            SubjectOffering offering,
            Long collegeId
    ) {

        return isProgramInCollege(
                offering.getProgramId(),
                collegeId
        );
    }

    private boolean isProgramInCollege(
            Long programId,
            Long collegeId
    ) {

        return programRepository.findById(programId)
                .map(program ->
                        departmentRepository
                                .findById(
                                        program.getDepartmentId()
                                )
                                .map(department ->
                                        collegeId.equals(
                                                department.getCollegeId()
                                        )
                                )
                                .orElse(false)
                )
                .orElse(false);
    }

    private boolean isAcademicSessionInCollege(
            Long academicSessionId,
            Long collegeId
    ) {

        return academicSessionRepository.findById(
                        academicSessionId
                )
                .map(session ->
                        collegeId.equals(
                                session.getCollegeId()
                        )
                )
                .orElse(false);
    }

    private boolean isSectionInCollege(
            Long sectionId,
            Long collegeId
    ) {

        return sectionRepository.findById(sectionId)
                .map(section ->
                        sectionBelongsToCollege(
                                section,
                                collegeId
                        )
                )
                .orElse(false);
    }

    private boolean sectionBelongsToCollege(
            Section section,
            Long collegeId
    ) {

        return isProgramInCollege(
                section.getProgramId(),
                collegeId
        )
                && isAcademicSessionInCollege(
                section.getAcademicSessionId(),
                collegeId
        );
    }

    private boolean subjectBelongsToProgram(
            Subject subject,
            Program program
    ) {

        return departmentRepository
                .findById(program.getDepartmentId())
                .map(programDepartment ->
                        programDepartment.getId().equals(
                                subject.getDepartmentId()
                        )
                )
                .orElse(false);
    }
}