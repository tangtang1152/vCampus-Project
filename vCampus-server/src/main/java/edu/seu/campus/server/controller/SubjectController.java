package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Subject;
import com.vCampus.service.ISubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    @Autowired
    private ISubjectService subjectService;

    @GetMapping
    public ApiResponse<List<Subject>> listAll() { return ApiResponse.ok(subjectService.getAllSubjects()); }

    @GetMapping("/{subjectId}")
    public ApiResponse<Subject> get(@PathVariable String subjectId) { return ApiResponse.ok(subjectService.getSubjectById(subjectId)); }

    @GetMapping("/exists/{subjectId}")
    public ApiResponse<Boolean> exists(@PathVariable String subjectId) { return ApiResponse.ok(subjectService.getSubjectById(subjectId) != null); }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Subject s) { return ApiResponse.ok(subjectService.addSubject(s)); }

    @PutMapping("/{subjectId}")
    public ApiResponse<Boolean> update(@PathVariable String subjectId, @RequestBody Subject s) { s.setSubjectId(subjectId); return ApiResponse.ok(subjectService.updateSubject(s)); }

    @DeleteMapping("/{subjectId}")
    public ApiResponse<Boolean> delete(@PathVariable String subjectId) { return ApiResponse.ok(subjectService.deleteSubject(subjectId)); }

    @GetMapping("/by-teacher/{teacherId}")
    public ApiResponse<List<Subject>> byTeacher(@PathVariable String teacherId) { return ApiResponse.ok(subjectService.getSubjectsByTeacherId(teacherId)); }

    @GetMapping("/by-name") // <-- 路径改为 /by-name
    public ApiResponse<List<Subject>> byName(@RequestParam(required = false) String name) { // <-- 使用 @RequestParam
        // 如果 name 为空或null，服务器端 Service 应该处理返回所有课程的逻辑
        List<Subject> subjects;
        if (name == null || name.isBlank()) {
            subjects = subjectService.getAllSubjects(); // 调用获取所有课程的方法
        } else {
            subjects = subjectService.getSubjectsByName(name); // 现有按名称模糊查询逻辑
        }
        return ApiResponse.ok(subjects);
    }
}



