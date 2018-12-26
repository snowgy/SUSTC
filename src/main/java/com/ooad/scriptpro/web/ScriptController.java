package com.ooad.scriptpro.web;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.ooad.scriptpro.model.Script;
import com.ooad.scriptpro.model.ScriptForm;
import com.ooad.scriptpro.model.ScriptFormText;
import com.ooad.scriptpro.model.User;
import com.ooad.scriptpro.service.FileService;
import com.ooad.scriptpro.service.ScriptService;
import com.ooad.scriptpro.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.Clob;

@Controller
public class ScriptController {
    @Autowired
    ScriptService scriptService;

    @Autowired
    TypeService typeService;

    @Autowired
    FileService fileService;

    @PostMapping("/createScript")
    public String SubmitScript(@ModelAttribute(value = "scriptForm") ScriptForm scriptForm,
                               HttpSession session) {
        Script script = new Script();
        script.setName(scriptForm.getName());
        script.setType(typeService.findServiceByName(scriptForm.getType()));
        script.setDescription(scriptForm.getDescription());
        String scriptContent;
        try{
            scriptContent = fileService.upload(scriptForm.getFile());
        }catch (Exception e){
            //return "upload fail";
            return "redirect:/myscripts";
        }
        if(scriptContent != null){
            try{
                Clob clob = new javax.sql.rowset.serial.SerialClob(scriptContent.toCharArray());
                script.setContent(clob);
                User user = (User)session.getAttribute("user");
                script.setAuthor(user.getUsername());
                scriptService.save(script);
                return "redirect:/myscripts";
            }catch (Exception e){
                //return "Error! String to clob fail!";
                return "redirect:/myscripts";
            }


        }else{
            //return "Error! Fail to upload!";
            return "redirect:/myscripts";
        }
    }

    @PostMapping("/createScriptText")
    public String SubmitScriptText(@ModelAttribute(value="scriptFormText")ScriptFormText scriptFormText,
                                   HttpSession session){
        System.out.println("call upload script controller");
        Script script = new Script();
        System.out.println(scriptFormText.getName());
        script.setName(scriptFormText.getName());

        script.setDescription(scriptFormText.getDescription());

        System.out.println("type:"+scriptFormText.getTypee());
        script.setType(typeService.findServiceByName(scriptFormText.getTypee()));
        String scriptContent = scriptFormText.getFile();
        if(scriptContent == null){
            System.out.println("null");
        }
        try{
            Clob clob = new javax.sql.rowset.serial.SerialClob(scriptContent.toCharArray());
            script.setContent(clob);
            User user = (User)session.getAttribute("user");
            script.setAuthor(user.getUsername());
            scriptService.save(script);
            System.out.println("save success");
            return "redirect:/myscripts";
        }catch (Exception e){
            return "redirect:/myscripts";
        }
    }
    @PostMapping("/updateScriptText")
    public String SubmitScriptText(@ModelAttribute(value="scriptFormText")ScriptFormText scriptFormText,
                                   @RequestParam long sid){
        System.out.println("call update script controller "+sid);

        Script script = new Script();
        script.setId(sid);
        System.out.println(scriptFormText.getName());
        script.setName(scriptFormText.getName());

        script.setDescription(scriptFormText.getDescription());

        System.out.println("type:"+scriptFormText.getTypee());
        script.setType(typeService.findServiceByName(scriptFormText.getTypee()));
        String scriptContent = scriptFormText.getFile();
        if(scriptContent == null){
            System.out.println("null");
        }
        try{
            Clob clob = new javax.sql.rowset.serial.SerialClob(scriptContent.toCharArray());
            script.setContent(clob);
            script.setAuthor(scriptService.findById(sid).getAuthor());
            scriptService.save(script);
            System.out.println("save success");
            return "redirect:/myscripts";
        }catch (Exception e){
            return "redirect:/myscripts";
        }
    }
}
