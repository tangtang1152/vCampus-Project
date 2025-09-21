package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Student;
import com.vCampus.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    @Autowired
    private IStudentService studentService;

    @GetMapping
    public ApiResponse<List<Student>> listAll() { return ApiResponse.ok(studentService.getAll()); }

    @GetMapping("/{studentId}")
    public ApiResponse<Student> get(@PathVariable String studentId) { return ApiResponse.ok(studentService.getBySelfId(studentId)); }

    @GetMapping("/exists/{studentId}")
    public ApiResponse<Boolean> exists(@PathVariable String studentId) { return ApiResponse.ok(studentService.exists(studentId)); }

    @GetMapping("/by-user/{userId}")
    public ApiResponse<Student> byUser(@PathVariable Integer userId) { return ApiResponse.ok(studentService.getByUserId(userId)); }

    @GetMapping("/full/{studentId}")
    public ApiResponse<Student> full(@PathVariable String studentId) { return ApiResponse.ok(studentService.getStudentFull(studentId)); }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Student s) { return ApiResponse.ok(studentService.add(s)); }

    @PutMapping("/{studentId}")
    public ApiResponse<Boolean> update(@PathVariable String studentId, @RequestBody Student s) { s.setStudentId(studentId); return ApiResponse.ok(studentService.update(s)); }

    @DeleteMapping("/{studentId}")
    public ApiResponse<Boolean> delete(@PathVariable String studentId) { return ApiResponse.ok(studentService.delete(studentId)); }

    @PutMapping("/only/{studentId}")
    public ApiResponse<Boolean> updateOnly(@PathVariable String studentId, @RequestBody Student s) { s.setStudentId(studentId); return ApiResponse.ok(studentService.updateStudentOnly(s)); }

    @DeleteMapping("/only/{studentId}")
    public ApiResponse<Boolean> deleteOnly(@PathVariable String studentId) { return ApiResponse.ok(studentService.deleteStudentOnly(studentId)); }

    @GetMapping("/by-class/{className}")
    public ApiResponse<List<Student>> byClass(@PathVariable String className) { return ApiResponse.ok(studentService.getStudentsByClass(className)); }
}



