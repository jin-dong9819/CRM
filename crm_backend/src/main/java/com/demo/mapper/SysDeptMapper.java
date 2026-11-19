package com.demo.mapper;

import com.demo.entity.SysDept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysDeptMapper {

    @Select("select * from sys_dept where del_flag = 0 order by parent_id, sort")
    List<SysDept> selectAll();

    @Select("select * from sys_dept where id = #{id} and del_flag = 0")
    SysDept selectById(Long id);

    @Insert("""
            insert into sys_dept (parent_id, ancestors, dept_name, dept_code, leader, phone, email, sort, status)
            values (#{parentId}, #{ancestors}, #{deptName}, #{deptCode}, #{leader}, #{phone}, #{email}, #{sort}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SysDept dept);

    @Update("""
            update sys_dept
            set parent_id = #{parentId},
                ancestors = #{ancestors},
                dept_name = #{deptName},
                dept_code = #{deptCode},
                leader    = #{leader},
                phone     = #{phone},
                email     = #{email},
                sort      = #{sort},
                status    = #{status},
                update_time = now()
            where id = #{id}
            """)
    void update(SysDept dept);

    /** 物理删除：删掉后部门编码可以重新使用 */
    @Delete("delete from sys_dept where id = #{id}")
    void deleteById(Long id);

    @Update("update sys_dept set status = #{status}, update_time = now() where id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 移动部门后，子孙的祖先链要跟着改：
     * 把以 oldPrefix 开头的 ancestors 换成 newPrefix
     */
    @Update("""
            update sys_dept
            set ancestors = concat(#{newPrefix}, substring(ancestors, char_length(#{oldPrefix}) + 1))
            where del_flag = 0
              and (ancestors = #{oldPrefix} or ancestors like concat(#{oldPrefix}, ',%'))
            """)
    void updateChildrenAncestors(@Param("oldPrefix") String oldPrefix,
                                 @Param("newPrefix") String newPrefix);
}
