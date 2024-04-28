package ${package};

import ${dtoImport};
import ${daoImport};
import ${entityImport};
import ${voImport};
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;



/**
* @author fangyonglong
* @version 1.0.0
* @date ${date}
* @description
*/
@Service
public class ${className}Service {

    @Autowired
    private ${className}Dao ${classObject}dao;

    /**
    * 新增方法
    * @param ${classObject}DTO
    */
    @Transactional(rollbackFor = Exception.class)
    public void addArticle(${className}DTO ${classObject}DTO) {
        ${className} ${classObject} = new ${className}();
        BeanUtils.copyProperties(${classObject}DTO,${classObject});
        ${classObject}.set${className}Id(UUID.randomUUID().toString());
        ${classObject}dao.saveOrUpdate(${classObject})
    }

    /**
    * 逻辑删除
    * @param ${classObject}Id
    */
    @Transactional(rollbackFor = Exception.class)
    public void addArticle(String ${classObject}Id) {
        ${classObject}dao.update(new LambdaUpdateWrapper<${className}>()
                .set(${className}::getDeleteTime,System.currentTimeMillis())
                .eq(${className}::get${className}Id,${classObject}Id));
    }
}