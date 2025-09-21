package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Department;
import com.vCampus.service.IDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    @Autowired
    private IDepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<Department>> listAll() { return ApiResponse.ok(departmentService.getAllDepartments()); }

    @GetMapping("/{departmentId}")
    public ApiResponse<Department> get(@PathVariable String departmentId) { return ApiResponse.ok(departmentService.getDepartmentById(departmentId)); }

    @GetMapping("/exists/{departmentId}")
    public ApiResponse<Boolean> exists(@PathVariable String departmentId) { return ApiResponse.ok(departmentService.departmentExists(departmentId)); }

    @GetMapping("/by-name/{name}")
    public ApiResponse<Department> byName(@PathVariable("name") String departmentName) { return ApiResponse.ok(departmentService.getDepartmentByName(departmentName)); }

    @GetMapping("/exists/by-name/{name}")
    public ApiResponse<Boolean> existsByName(@PathVariable("name") String departmentName) { return ApiResponse.ok(departmentService.departmentNameExists(departmentName)); }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Department d) { return ApiResponse.ok(departmentService.addDepartment(d)); }

    @PutMapping("/{departmentId}")
    public ApiResponse<Boolean> update(@PathVariable String departmentId, @RequestBody Department d) { d.setDepartmentId(departmentId); return ApiResponse.ok(departmentService.updateDepartment(d)); }

    @DeleteMapping("/{departmentId}")
    public ApiResponse<Boolean> delete(@PathVariable String departmentId) { return ApiResponse.ok(departmentService.deleteDepartment(departmentId)); }
}



