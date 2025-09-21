package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Choose;
import com.vCampus.entity.Subject;
import com.vCampus.service.IChooseService;
import com.vCampus.service.ServiceResult; // 导入 ServiceResult
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chooses")
public class ChooseController {

    @Autowired
    private IChooseService chooseService;

    @PostMapping("/choose")
    public ApiResponse<ServiceResult> choose(@RequestBody Map<String, String> body) { // 返回类型已修改
        // 直接返回 ServiceResult，不再包装成 Boolean
        return ApiResponse.ok(chooseService.chooseSubject(body.get("studentId"), body.get("subjectId"))); 
    }

    @PostMapping("/admin-choose")
    public ApiResponse<ServiceResult> adminChoose(@RequestBody Map<String, Object> body) { // 返回类型已修改
        String studentId = String.valueOf(body.get("studentId"));
        String subjectId = String.valueOf(body.get("subjectId"));
        boolean ignore = body.get("ignoreTimeConflict") != null && Boolean.parseBoolean(String.valueOf(body.get("ignoreTimeConflict")));
        // 直接返回 ServiceResult
        return ApiResponse.ok(chooseService.adminAssistChooseSubject(studentId, subjectId, ignore));
    }

    @PostMapping("/drop/{selectid}")
    public ApiResponse<ServiceResult> drop(@PathVariable String selectid) { // 返回类型已修改
        // 直接返回 ServiceResult
        return ApiResponse.ok(chooseService.dropSubject(selectid));
    }

    @GetMapping("/by-student/{studentId}")
    public ApiResponse<List<Subject>> byStudent(@PathVariable String studentId) {
        return ApiResponse.ok(chooseService.getStudentSubjects(studentId));
    }

    @GetMapping("/by-subject/{subjectId}")
    public ApiResponse<List<Choose>> bySubject(@PathVariable String subjectId) {
        return ApiResponse.ok(chooseService.getSubjectChooses(subjectId));
    }

    @GetMapping("/is-chosen")
    public ApiResponse<Boolean> isChosen(@RequestParam String studentId, @RequestParam String subjectId) {
        return ApiResponse.ok(chooseService.isSubjectChosen(studentId, subjectId));
    }

    @GetMapping("/{selectid}")
    public ApiResponse<Choose> detail(@PathVariable String selectid) {
        return ApiResponse.ok(chooseService.getChooseDetail(selectid));
    }

    @GetMapping("/find")
    public ApiResponse<Choose> findByStuAndSub(@RequestParam String studentId, @RequestParam String subjectId) {
        return ApiResponse.ok(chooseService.findByStudentAndSubject(studentId, subjectId));
    }
}