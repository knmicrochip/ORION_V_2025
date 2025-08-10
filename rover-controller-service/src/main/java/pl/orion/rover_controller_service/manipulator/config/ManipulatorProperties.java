package pl.orion.rover_controller_service.manipulator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "manipulator")
public record ManipulatorProperties(
        String eventType,
        DownstreamTopics downstream,
        UpstreamTopics upstream,
        JointsProperties joints
) {
    public record DownstreamTopics(
            String inbound,
            String outbound
    ) {}

    public record UpstreamTopics(
            String inbound
    ) {}

    public record JointsProperties(
            JointProperties turret_rotation,
            JointProperties forearm_flex,
            JointProperties arm_flex,
            JointProperties gripper_flex,
            JointProperties gripper_rotation,
            JointProperties end_effector_flex
    ) {}

    public record JointProperties(
            double max_ang_v,
            double min_pos,
            double max_pos
    ) {}
}
