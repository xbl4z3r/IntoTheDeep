package me.xbl4z3r.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;

import org.rowlandhall.meepmeep.MeepMeep;
import org.rowlandhall.meepmeep.roadrunner.DefaultBotBuilder;
import org.rowlandhall.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting
{
	public static void main(String[] args)
	{
		MeepMeep meepMeep = new MeepMeep(800);

		RoadRunnerBotEntity robot = new DefaultBotBuilder(meepMeep)
				// Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
				.setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
				.followTrajectorySequence(drive -> {
					// Create a trajectory for the bot to follow
					return drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.PI / 6))
							.setTangent(Math.PI)
							.splineToLinearHeading(new Pose2d(5, -15, Math.PI / 2), 0)
							.build();
				});

		meepMeep.setBackground(MeepMeep.Background.FIELD_INTOTHEDEEP_JUICE_DARK)
				.setDarkMode(true)
				.addEntity(robot)
				.start();
	}
}
