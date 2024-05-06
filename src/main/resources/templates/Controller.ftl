package ${package};

import ${dtoImport};
import ${voImport};
import ${entityImport};
import ${serviceImport};
import alp.starcode.common.mybatis.page.Pagination;
import alp.starcode.common.web.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



/**
* @author ${author}
* @version 1.0.0
* @date ${date}
* @description
*/

@Api(tags = "${entityDescription}相关接口")
@RestController
@RequestMapping("${classObject}")
public class ${className}Controller {

    @Autowired
    private ${className}Service ${classObject}Service;

    @GetMapping("page${className}.do")
    @ApiOperation("分页查询${entityDescription}信息")
    @ApiImplicitParams({
        @ApiImplicitParam(value = "searchKey", name = "关键词搜索", dataType = "string", paramType = "query", required = false)
    })
    public Result<Pagination<${className}VO>> page${className}(Pagination<${className}VO> page, String searchKey){
        return Result.success(${classObject}Service.page${className}(page,searchKey));
    }

    @GetMapping("list${className}.do")
    @ApiOperation("列表查询${entityDescription}信息")
    @ApiImplicitParams({
        @ApiImplicitParam(value = "searchKey", name = "关键词搜索", dataType = "string", paramType = "query", required = false)
    })
    public Result<List<${className}VO>> list${className}(String searchKey){
        return Result.success(${classObject}Service.list${className}(searchKey));
    }

    @GetMapping("get${className}.do")
    @ApiOperation("根据Id查询${entityDescription}信息")
    @ApiImplicitParams({
        @ApiImplicitParam(value = "${idField}", name = "${idDescription}", dataType = "string", paramType = "query", required = true)
    })
    public Result<${className}> get${className}(String ${idField}){
        return Result.success(${classObject}Service.get${className}(${idField}));
    }

    @PostMapping("add${className}.do")
    @ApiOperation("新增${entityDescription}信息")
    public Result<Object> add${className}(${className}DTO ${classObject}DTO){
        ${classObject}Service.add${className}(${classObject}DTO);
        return Result.success();
    }

    @PostMapping("update${className}.do")
    @ApiOperation("修改${entityDescription}信息")
    public Result<Object> update${className}(${className}DTO ${classObject}DTO){
         ${classObject}Service.update${className}(${classObject}DTO);
         return Result.success();
    }

    @PostMapping("delete${className}.do")
    @ApiOperation("删除${entityDescription}信息")
    @ApiImplicitParams({
        @ApiImplicitParam(value = "${idField}", name = "${idDescription}", dataType = "string", paramType = "query", required = true)
    })
    public Result<Object> delete${className}(String ${idField}){
         ${classObject}Service.delete${className}(${idField});
         return Result.success();
     }
}