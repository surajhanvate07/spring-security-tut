package com.suraj.security.hospitalManagement.repository;

import com.suraj.security.hospitalManagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}