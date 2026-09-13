package com.attendance.system.service;

import com.attendance.system.entity.College;
import com.attendance.system.entity.CollegeStatus;
import com.attendance.system.repository.CollegeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollegeService {

    private final CollegeRepository collegeRepository;

    public CollegeService(CollegeRepository collegeRepository) {
        this.collegeRepository = collegeRepository;
    }

    // Get all colleges
    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    // Get college by ID
    public Optional<College> getCollegeById(Long id) {
        return collegeRepository.findById(id);
    }

    // Create new college
    // Create new college
    public College createCollege(College college) {

        // If status is not provided by the client,
        // use ACTIVE as the default application-level value.
        if (college.getStatus() == null) {
            college.setStatus(CollegeStatus.active);
        }

        // Prevent duplicate college codes before saving.
        if (collegeRepository.existsByCodeIgnoreCase(college.getCode())) {
            throw new IllegalStateException(
                    "College with code '" + college.getCode() + "' already exists"
            );
        }

        return collegeRepository.save(college);
    }

    // Update existing college
    public Optional<College> updateCollege(Long id, College updatedCollege) {

        return collegeRepository.findById(id)
                .map(existingCollege -> {

                    // Prevent duplicate college codes during update.
                    // The current college's own ID is excluded from the check.
                    if (collegeRepository.existsByCodeIgnoreCaseAndIdNot(
                            updatedCollege.getCode(), id)) {
                        throw new IllegalStateException(
                                "College with code '" + updatedCollege.getCode() + "' already exists"
                        );
                    }

                    existingCollege.setName(updatedCollege.getName());
                    existingCollege.setCode(updatedCollege.getCode());
                    existingCollege.setUniversity(updatedCollege.getUniversity());
                    existingCollege.setAddress(updatedCollege.getAddress());
                    existingCollege.setContactEmail(updatedCollege.getContactEmail());
                    existingCollege.setContactPhone(updatedCollege.getContactPhone());

                    return collegeRepository.save(existingCollege);
                });
    }

    // Change college status
    public Optional<College> updateStatus(Long id, CollegeStatus status) {

        return collegeRepository.findById(id)
                .map(college -> {
                    college.setStatus(status);
                    return collegeRepository.save(college);
                });
    }
}
