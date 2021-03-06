package com.ooad.scriptpro.service;

import com.ooad.scriptpro.api.ScriptRepository;
import com.ooad.scriptpro.model.Script;
import com.ooad.scriptpro.model.User;
import com.ooad.scriptpro.service.docker.Container;
import com.ooad.scriptpro.service.docker.ContainerRun;
import com.ooad.scriptpro.service.docker.config.ScriptLang;
import com.ooad.scriptpro.web.utils.TypeAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ScriptServiceImpl implements ScriptService {
    @Autowired
    ScriptRepository scriptRepository;

    @Override
    public Script findById(long id) {
        return scriptRepository.findScriptById(id);
    }

    @Override
    public Script findByScriptName(String scriptName) {
        return scriptRepository.findScriptByName(scriptName);
    }

    @Override
    public void save(Script script) {
        scriptRepository.save(script);
    }

    @Override
    public void deleteScript(Script script)
    {
        Set<User> users = script.getUsers();
        for(User user: users){
            users.remove(script);
        }
        scriptRepository.delete(script);
    }

    @Override
    public void deleteById(long id) {
        Script script = scriptRepository.findScriptById(id);
        Set<User> users = script.getUsers();
        for(User user: users){
            users.remove(script);
        }
        scriptRepository.deleteById(id);
    }

    @Override
    public void deleteByScriptName(String scriptName) {
        Script script = scriptRepository.findScriptByName(scriptName);
        Set<User> users = script.getUsers();
        for(User user: users){
            users.remove(script);
        }
        scriptRepository.deleteScriptByName(scriptName);
    }

    @Override
    public List<User> getAuthors(Script script) {
        return new ArrayList<>(script.getUsers());
    }

    @Override
    public List<Script> getTopFivePopular() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Script> scripts = scriptRepository.findAllByOrderByPopularPointsDesc(pageable);
        return scripts;

    }

    @Override
    public List<Script> getTopFiveLatest() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Script> scripts = scriptRepository.findAllByOrderByUpdateTimeDesc(pageable);
        return scripts;
    }

    @Override
    public List<Script> vagueSearch(String query) {
        return scriptRepository.findAllByNameLike('%' + query + "%");
    }

    @Override
    public String getScriptContentById(long id) throws IOException, SQLException {
        Script script = findById(id);
        BufferedReader br = new BufferedReader(script.getContent().getCharacterStream());
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    public ContainerRun run(String apiPrefix, String typename, int id, String args_str) {
        TypeAdapter typeAdapter = new TypeAdapter();
        ScriptLang scriptLang = typeAdapter.toScriptLang(typename);
        String args[] =  args_str.split(" ");
        try{
            Container container = new Container(apiPrefix, scriptLang, id, args);
            container.execCreateContainer();
            int ret = container.execRunContainer();
            ContainerRun containerRun = new ContainerRun();

            if (ret == 0) {
                containerRun.setRet(0);
                containerRun.setOutput(container.getOutput());
                return containerRun;
            } else {
                containerRun.setRet(1);
                containerRun.setOutput(container.getError());
                return containerRun;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
