package ${package};

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModelProperty;

/**
* @author fangyonglong
* @version 1.0.0
* @date ${date}
* @description
*/
@Data
public class ${className}DTO{

<#list fieldList as field>
    <#if field.fieldAnnotation?? && field.fieldAnnotation?has_content>
        <#list field.fieldAnnotation as annotation>
    ${annotation}
        </#list>
    </#if>
    private ${field.fieldType} ${field.fieldName};

</#list>

}
