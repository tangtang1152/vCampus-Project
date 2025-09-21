package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Teacher;
import com.vCampus.service.ITeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    @Autowired
    private ITeacherService teacherService;

    @GetMapping
    public ApiResponse<List<Teacher>> listAll() { return ApiResponse.ok(teacherService.getAll()); }

    @GetMapping("/{teacherId}")
    public ApiResponse<Teacher> get(@PathVariable String teacherId) { return ApiResponse.ok(teacherService.getBySelfId(teacherId)); }

    @GetMapping("/exists/{teacherId}")
    public ApiResponse<Boolean> exists(@PathVariable String teacherId) { return ApiResponse.ok(teacherService.exists(teacherId)); }

    @GetMapping("/by-user/{userId}")
    public ApiResponse<Teacher> byUser(@PathVariable Integer userId) { return ApiResponse.ok(teacherService.getByUserId(userId)); }

    @GetMapping("/full/{teacherId}")
    public ApiResponse<Teacher> full(@PathVariable String teacherId) { return ApiResponse.ok(teacherService.getTeacherFull(teacherId)); }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Teacher t) { return ApiResponse.ok(teacherService.add(t)); }

    @PutMapping("/{teacherId}")
    public ApiResponse<Boolean> update(@PathVariable String teacherId, @RequestBody Teacher t) { t.setTeacherId(teacherId); return ApiResponse.ok(teacherService.update(t)); }

    @DeleteMapping("/{teacherId}")
    public ApiResponse<Boolean> delete(@PathVariable String teacherId) { return ApiResponse.ok(teacherService.delete(teacherId)); }

    @PutMapping("/only/{teacherId}")
    public ApiResponse<Boolean> updateOnly(@PathVariable String teacherId, @RequestBody Teacher t) { t.setTeacherId(teacherId); return ApiResponse.ok(teacherService.updateTeacherOnly(t)); }

    @DeleteMapping("/only/{teacherId}")
    public ApiResponse<Boolean> deleteOnly(@PathVariable String teacherId) { return ApiResponse.ok(teacherService.deleteTeacherOnly(teacherId)); }

    @GetMapping("/by-dept/{departmentId}")
    public ApiResponse<List<Teacher>> byDept(@PathVariable String departmentId) { return ApiResponse.ok(teacherService.getTeachersByDepartment(departmentId)); }
}



