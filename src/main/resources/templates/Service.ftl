package ${package};

import ${dtoImport};
import ${voImport};
import ${daoImport};
import ${entityImport};
import alp.starcode.common.mybatis.page.Pagination;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;



/**
* @author ${author}
* @version 1.0.0
* @date ${date}
* @description
*/

@Service
public class ${className}Service {

    @Autowired
    private ${className}Dao ${classObject}Dao;

    /**
    * 分页查询
    * @param page
    * @param searchKey
    */
    public Pagination<${className}Vo> page${className}(Pagination<${className}Vo> page, String searchKey){
        MPJLambdaWrapper<${className}> wrapper = new MPJLambdaWrapper<>();

        return ${classObject}Dao.selectJoinListPage(page, ${className}Vo.class, wrapper);
    }

    /**
    * 列表查询
    * @param searchKey
    */
    public List<${className}Vo> list${className}(String searchKey){
         MPJLambdaWrapper<${className}> wrapper = new MPJLambdaWrapper<>();

         return ${classObject}Dao.selectJoinList(${className}Vo.class, wrapper);
    }

    /**
    * 根据Id查询
    * @param ${idField}
    */
    @Transactional(rollbackFor = Exception.class)
    public ${className} get${className} (String ${idField}) {
        return ${classObject}Dao.getById(${idField});
    }

    /**
    * 新增方法
    * @param ${classObject}Dto
    */
    @Transactional(rollbackFor = Exception.class)
    public void add${className}(${className}Dto ${classObject}Dto) {
        ${className} ${classObject} = new ${className}();
        BeanUtils.copyProperties(${classObject}Dto,${classObject});
        ${classObject}.set${IdField}(UUID.randomUUID().toString());
        ${classObject}Dao.saveOrUpdate(${classObject});
    }

    /**
    * 修改方法
    * @param ${classObject}Dto
    */
    @Transactional(rollbackFor = Exception.class)
    public void update${className}(${className}Dto ${classObject}Dto) {
        ${className} ${classObject} = new ${className}();
        BeanUtils.copyProperties(${classObject}Dto,${classObject});
        ${classObject}Dao.updateById(${classObject});
    }

    /**
    * 删除方法
    * @param ${idField}
    */
    @Transactional(rollbackFor = Exception.class)
    public void delete${className}(String ${idField}) {
        ${classObject}Dao.removeById(${idField});
    }
}