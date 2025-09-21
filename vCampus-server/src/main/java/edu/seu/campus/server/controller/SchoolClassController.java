package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.SchoolClass;
import com.vCampus.service.ISchoolClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
public class SchoolClassController {

    @Autowired
    private ISchoolClassService schoolClassService;

    @GetMapping
    public ApiResponse<List<SchoolClass>> listAll() { return ApiResponse.ok(schoolClassService.getAllClasses()); }

    @GetMapping("/{classId}")
    public ApiResponse<SchoolClass> get(@PathVariable String classId) { return ApiResponse.ok(schoolClassService.getClassById(classId)); }

    @GetMapping("/exists/{classId}")
    public ApiResponse<Boolean> exists(@PathVariable String classId) { return ApiResponse.ok(schoolClassService.classExists(classId)); }

    @GetMapping("/by-department/{departmentId}")
    public ApiResponse<List<SchoolClass>> byDepartment(@PathVariable String departmentId) { return ApiResponse.ok(schoolClassService.getClassesByDepartmentId(departmentId)); }

    @GetMapping("/by-name/{name}")
    public ApiResponse<List<SchoolClass>> byName(@PathVariable("name") String className) { return ApiResponse.ok(schoolClassService.getClassesByClassName(className)); }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody SchoolClass c) { return ApiResponse.ok(schoolClassService.addClass(c)); }

    @PutMapping("/{classId}")
    public ApiResponse<Boolean> update(@PathVariable String classId, @RequestBody SchoolClass c) { c.setClassId(classId); return ApiResponse.ok(schoolClassService.updateClass(c)); }

    @DeleteMapping("/{classId}")
    public ApiResponse<Boolean> delete(@PathVariable String classId) { return ApiResponse.ok(schoolClassService.deleteClass(classId)); }

    @GetMapping("/exists/by-name/{name}")
    public ApiResponse<Boolean> classNameExists(@PathVariable("name") String className) { return ApiResponse.ok(schoolClassService.classNameExists(className)); }
}

