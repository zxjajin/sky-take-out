package com.sky.dto;

import lombok.Data;

/**
 * @author ajin
 * @create 2024-07-24 19:38
 */
@Data
public class EmployeePasswordDTO {
    private Long empId;
    private String newPassword;
    private String oldPassword;
}
