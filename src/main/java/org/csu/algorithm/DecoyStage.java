package org.csu.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.csu.entity.Task;
import org.csu.entity.TaskType;

import java.util.List;

@Slf4j
public class DecoyStage {

    // 诱饵任务与目标任务的质量转化比
    public static final double DECOY_QUALITY_TRANSFER = 0.99;

    // 诱饵任务与目标任务的报价转化比
    public static final double DECOY_BID_TRANSFER = 0.93;

    /**
     * 添加诱饵任务
     * @param taskList
     */
    public void addDecoyTask(List<Task> taskList){
        log.info("-----诱饵任务加入-----");
        double size = 0;
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            if (TaskType.Target.equals(task.getTaskType())){
                Task decoy = new Task();
                decoy.setTaskQuality(task.getTaskQuality() * DECOY_QUALITY_TRANSFER)
                        .setTaskBid(task.getTaskBid() * DECOY_BID_TRANSFER)
                        .setTaskType(TaskType.Decoy);

                task.setDecoyTask(decoy);
                size++;
            }
        }
        log.info("-----诱饵任务加入数量[{}]-----",size);
        log.info("-----诱饵任务完成-----");
    }

}
