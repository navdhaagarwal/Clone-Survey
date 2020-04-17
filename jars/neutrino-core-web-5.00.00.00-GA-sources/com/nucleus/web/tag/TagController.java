package com.nucleus.web.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nucleus.html.util.HtmlUtils;

import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.tag.entity.ClassificationTag;
import com.nucleus.tag.service.TagService;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> manas.grover Add documentation to class
 */
@Controller
@RequestMapping(value = "/tag")
public class TagController extends BaseController {

    @Inject
    @Named(value = "tagService")
    TagService tagService;

    /**
     * TODO -> manas.grover Add comment to method
     * @param map
     * @param tagName
     * @param entityClass
     * @param id
     * @param request
     * @return
     * @throws ClassNotFoundException
     */
    @PreAuthorize("hasAuthority('AUTHORITY_ADD_TAG')")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/addTag/{id}/{taskId}")
    public String addTag(ModelMap map, @RequestParam("tagName") String tagName, @PathVariable("taskId") String taskId,
            @RequestParam("currentEntityUri") String entityClass, @PathVariable Long id, HttpServletRequest request)
            throws ClassNotFoundException {
        if(taskId!=null && !taskId.equals("null")) {
            tagService.addClassificationTagToEntityWithTaskId(new EntityId((Class<Entity>) Class.forName(entityClass), id), tagName, taskId);
        }else{
            tagService.addClassificationTagToEntity(new EntityId((Class<Entity>) Class.forName(entityClass), id), tagName);
        }
        return "tagPage";
    }

    /**
     * TODO -> manas.grover Add comment to method
     * @param map
     * @param tagName
     * @param entityClass
     * @param id
     * @param request
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('AUTHORITY_ADD_TAG') or hasAuthority('AUTHORITY_DELETE_TAG')")
    @RequestMapping(value = "/removeTag/{id}")
    public String removeTag(ModelMap map, @RequestParam("tagName") String tagName,
            @RequestParam("currentEntityUri") String entityClass, @PathVariable Long id, HttpServletRequest request)
            throws ClassNotFoundException {

        tagService
                .removeClassificationTagForEntityUri(new EntityId((Class<Entity>) Class.forName(entityClass), id), tagName);
        return "tagPage";

    }

    @PreAuthorize("hasAuthority('TAG_CLOUD')")
    @RequestMapping(value = "/loadheatMap")
    public String tagForHeatMap(ModelMap map) {
        return "heatMap";

    }

    @PreAuthorize("hasAuthority('TAG_CLOUD')")
    @RequestMapping(value = "/linkHeatMap/{index}")
    public String tagForLinkHeatMap(@PathVariable("index") String index, ModelMap map) {
        map.put("index", HtmlUtils.htmlEscape(index));
        map.put("tagList", tagService.fetchAllClassificationTags());
        return "heatMapLinkPage";
    }

    @PreAuthorize("hasAuthority('TAG_CLOUD')")
    @RequestMapping(value = "/heatMap")
    public @ResponseBody
    String createTagForHeatMap(ModelMap map) {
        JSONSerializer serializer = new JSONSerializer();
        Map<String, Integer> tagCloud = new HashMap<String, Integer>();
        List<ClassificationTag> tagList = tagService.fetchAllClassificationTags();
        for (ClassificationTag tag : tagList) {
            tagCloud.put(tag.getTagName(), tag.getEntityUris().size());
        }
        return serializer.serialize(tagCloud);

    }

    /**
     * TODO -> manas.grover Add comment to method
     * @param map
     * @param entityClass
     * @param id
     * @param request
     * @return
     * @throws ClassNotFoundException
     */
    @PreAuthorize("hasAuthority('AUTHORITY_ADD_TAG') or hasAuthority('AUTHORITY_VIEW_TAG')")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/retrieveTag/{id}/{taskId}")
    public String retrieveTags(ModelMap map, @RequestParam("currentEntityUri") String entityClass, @PathVariable Long id, @PathVariable String taskId,
            HttpServletRequest request) throws ClassNotFoundException {

        List<String> tagList;
        if(taskId!=null && !taskId.equals("null")) {
            tagList = tagService.getAllClassificationTagsForUriByTaskId(new EntityId((Class<Entity>) Class
                    .forName(entityClass), id), taskId);
        }else {
            tagList = tagService.getAllClassificationTagsForUri(new EntityId((Class<Entity>) Class
                    .forName(entityClass), id));
        }
        map.addAttribute("tagList", tagList);
        return "tagPage";

    }

    /**
     * TODO -> manas.grover Add comment to method
     * @param map
     * @param tagName
     * @param request
     * @return
     * @throws ClassNotFoundException 
     */
    /* @RequestMapping(value = "/autocomplete")
     public String autocompleteTagName(ModelMap map, @RequestParam("currentEntityUri") String entityClass,
             @PathVariable Long id, @RequestParam("tagName") String tagName, HttpServletRequest request)
             throws ClassNotFoundException {
         @SuppressWarnings("unchecked")
         List<ClassificationTag> tagSuggestion = tagService.autocompleteTagName(
                 new EntityId((Class<Entity>) Class.forName(entityClass), id), tagName);

         map.addAttribute("tagSuggestion", tagSuggestion);
         return "tagTab";

     }*/
    @PreAuthorize("hasAuthority('AUTHORITY_ADD_TAG')")
    @RequestMapping(value = "/autocomplete")
    public String autocompleteTagName(ModelMap map, @RequestParam String value, @RequestParam String itemVal,
            @RequestParam String searchCol, @RequestParam String className, @RequestParam boolean loadApprovedEntityFlag,
            @RequestParam String i_label, @RequestParam String idCurr, @RequestParam String content_id,
            @RequestParam int page, HttpServletRequest req, @RequestParam String entity_id,
            @RequestParam String currentEntityClassName) throws ClassNotFoundException {

        @SuppressWarnings("unchecked")
        List<Map<String, String>> list = tagService.autocompleteTagName(
                new EntityId((Class<Entity>) Class.forName(currentEntityClassName), Long.valueOf(entity_id)), value);
        if (list.size() > 0) {
            map.put("size", list.size());
            map.put("page", page);

            // if remainder is 1 when size of list is divided by 3
            if (list.size() / 3 == page && list.size() % 3 == 1)
                list = list.subList(3 * page, 3 * page + 1);

            // if remainder is 2 when size of list is divided by 3
            else if (list.size() / 3 == page && list.size() % 3 == 2)
                list = list.subList(3 * page, 3 * page + 2);

            else
                list = list.subList(3 * page, 3 * page + 3);
        }

        map.put("data", list);
        map.put("idCurr", idCurr);
        map.put("i_label", i_label);
        map.put("content_id", content_id);

        return "autocomplete";

    }

}
