package com.openquartz.sequence.generator.example.controller;

import com.openquartz.sequence.core.expr.SequenceGenerateService;
import com.openquartz.sequence.core.expr.cmd.AssignExtParam;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 序列Controller
 *
 * @author svnee
 **/
@RestController
@RequestMapping("/seq")
public class SequenceController {

    @Resource
    private SequenceGenerateService sequenceGenerateService;

    @GetMapping("/get")
    public String get(@RequestParam("code") String code) {
        return sequenceGenerateService.generateCode(code, AssignExtParam.create().set("w", "123"));
    }


}
