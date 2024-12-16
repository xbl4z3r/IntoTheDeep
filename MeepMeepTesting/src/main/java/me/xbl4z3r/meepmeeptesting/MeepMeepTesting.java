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
					return drive.trajectorySequenceBuilder(new Pose2d(0, 0, 0))
							.setTangent(Math.PI)
							.splineToConstantHeading(centimetersToInches(new Vector2d(-78, 20)), Math.PI)
							.setTangent(0)
							.splineToLinearHeading(centimetersToInches(new Pose2d(-5, -40)), Math.PI * 3 / 4)
							.build();
				});

		meepMeep.setBackground(MeepMeep.Background.FIELD_INTOTHEDEEP_JUICE_DARK)
				.setDarkMode(true)
				.addEntity(robot)
				.start();
	}

	private static Vector2d centimetersToInches(Vector2d pose) { return pose.times(1 / 2.54); }
	private static Pose2d centimetersToInches(Pose2d pose) { return new Pose2d(centimetersToInches(pose.vec()), pose.getHeading()); }
}
