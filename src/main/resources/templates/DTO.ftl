package ${package};


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
* @author ${author}
* @version 1.0.0
* @date ${date}
* @description
*/
@Data
public class ${className}Dto{

<#list fieldList as field>
    <#if field.fieldAnnotation?? && field.fieldAnnotation?has_content>
        <#list field.fieldAnnotation as annotation>
            <#if annotation?string != "@TableId">
    ${annotation}
            </#if>
        </#list>
    </#if>
    private ${field.fieldType} ${field.fieldName};

</#list>

}
