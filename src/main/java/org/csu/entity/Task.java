package org.csu.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Task {

    // 任务唯一标识
    private Integer taskId;

    // 任务分类
    private TaskType taskType;

    // 任务价值
    private double taskValue;

    // 任务报价
    private double taskBid;

    // 任务质量
    private double taskQuality;

    // 诱饵任务(任务为目标任务则不为null)
    private Task decoyTask;

}
