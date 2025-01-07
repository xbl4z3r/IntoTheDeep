package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.InputSystem;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.Utilities;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;

import java.util.List;

@TeleOp(name = " \uD83D\uDC79Fane\uD83D\uDC79 Op22222 ", group = "TeleOp")
public final class FaneOp2 extends BaseOpMode
{
	private InputSystem driveInput, armInput;
	private MecanumDrive mecanumDrive;
	private DcMotorEx lift;
	private Servo extendoServo1, extendoServo2, tumblerServo, smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw, fixedClaw, susServo1, susServo2;

	Utilities.Alliance alliance;

	ColorBlobLocatorProcessor processor;
	VisionPortal portal;

	public static class Constants
	{
		public static float EXTENDO1_RETRACTED = 0;
		public static float EXTENDO2_RETRACTED = 1;
		public static float EXTENDO1_EXTENDED = 1;
		public static float EXTENDO2_EXTENDED = 0;

		public static float TUMBLER_DELIVER = 0f;
		public static float TUMBLER_IDLE = 0.145f;
		public static float TUMBLER_DETECT = 0.74f;
		public static float TUMBLER_PICKUP = 0.82f;

		public static float SMALL_TUMBLER_DELIVER = 0.32f;
		public static float SMALL_TUMBLER_DETECT = 1f;
		public static float SMALL_TUMBLER_PICKUP = 0.88f;

		public static float ROTATOR_BASE = 0.5f;

		public static float CLAW_OPEN = 0.45f;
		public static float CLAW_CLOSED = 0.1f;

		public static int LIFT_IDLE = 0;
		public static int LIFT_SUSPEND = 770;
		public static int LIFT_OFFSET = 270;

		// BASKET_LOW, BASKET_HIGH, SUBMERSIBLE_HIGH
		public static int[] LIFT_LEVELS = { 670, 1800, 950 };

		public static float SCORE_TUMBLER_IDLE = 0.09f;
		public static float SCORE_TUMBLER_BASKET = 0.8f;

		public static float SCORE_CLAW_OPEN = 0.8f;
		public static float SCORE_CLAW_CLOSED = 0.6f;

		public static float FIXED_CLAW_OPEN = 0.75f;
		public static float FIXED_CLAW_CLOSED = 0.45f;

		public static float SUS_SERVO1_UNLOCKED = 0.5f;
		public static float SUS_SERVO1_LOCKED = 0.39f;
		public static float SUS_SERVO2_UNLOCKED = 0.325f;
		public static float SUS_SERVO2_LOCKED = 0.39f;
	}

	private static class Keybindings
	{
		public static class Arm
		{
			public static final InputSystem.Key BASKET_LOW_KEY = new InputSystem.Key("dpad_down");
			public static final InputSystem.Key BASKET_HIGH_KEY = new InputSystem.Key("dpad_up");
			public static final InputSystem.Key CHAMBER_HIGH_KEY = new InputSystem.Key("dpad_left");
			public static final InputSystem.Key PRIMARY_KEY = new InputSystem.Key("a");
			public static final InputSystem.Key SUSPEND_KEY = new InputSystem.Key("y");
			public static final InputSystem.Key CANCEL_SUSPEND_KEY = new InputSystem.Key("x");
		}

		public static class Drive
		{
			public static final InputSystem.Key TURBO_KEY = new InputSystem.Key("right_bumper");
			public static final InputSystem.Key SUPPRESS_KEY = new InputSystem.Key("left_bumper");
			public static final InputSystem.Axis DRIVE_X = new InputSystem.Axis("left_stick_x");
			public static final InputSystem.Axis DRIVE_Y = new InputSystem.Axis("left_stick_y");
			public static final InputSystem.Axis DRIVE_ROT_L = new InputSystem.Axis("left_trigger");
			public static final InputSystem.Axis DRIVE_ROT_R = new InputSystem.Axis("right_trigger");
			public static final InputSystem.Key EXTENDO_FULL_KEY = new InputSystem.Key("dpad_up");
			public static final InputSystem.Key EXTENDO_HALF_KEY = new InputSystem.Key("dpad_left");
			public static final InputSystem.Key EXTENDO_ZERO_KEY = new InputSystem.Key("dpad_down");
			public static final InputSystem.Key AUTO_ROTATOR_KEY = new InputSystem.Key("y");
			public static final InputSystem.Axis ROTATOR_ANGLE = new InputSystem.Axis("right_stick_x");
			public static final InputSystem.Key GRAB_TRANSFER_KEY = new InputSystem.Key("a");
			public static final InputSystem.Key GRAB_HOLD_KEY = new InputSystem.Key("b");
			public static final InputSystem.Key GRAB_WALL_KEY = new InputSystem.Key("x");
		}
	}

	@Override
	protected void OnInitialize()
	{
		driveInput = new InputSystem(gamepad1);
		armInput = new InputSystem(gamepad2);

		mecanumDrive = new MecanumDrive(hardwareMap);

		lift = hardwareMap.get(DcMotorEx.class, "lift");
		lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift.setTargetPosition(0);
		lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift.setPower(1);

		extendoServo1 = hardwareMap.get(Servo.class, "extendo1");
		extendoServo2 = hardwareMap.get(Servo.class, "extendo2");
		tumblerServo = hardwareMap.get(Servo.class, "tumbler");
		smallTumbler = hardwareMap.get(Servo.class, "sTumbler");
		rotatorServo = hardwareMap.get(Servo.class, "rotator");
		clawServo = hardwareMap.get(Servo.class, "claw");
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");
		fixedClaw = hardwareMap.get(Servo.class, "fixedClaw");
		susServo1 = hardwareMap.get(Servo.class, "susServo1");
		susServo2 = hardwareMap.get(Servo.class, "susServo2");

		extendoServo1.setPosition(Constants.EXTENDO1_RETRACTED);
		extendoServo2.setPosition(Constants.EXTENDO2_RETRACTED);
		tumblerServo.setPosition(Constants.TUMBLER_IDLE);
		smallTumbler.setPosition(Constants.SMALL_TUMBLER_DELIVER);
		rotatorServo.setPosition(Constants.ROTATOR_BASE);
		clawServo.setPosition(Constants.CLAW_OPEN);
		scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
		scoreClaw.setPosition(Constants.SCORE_CLAW_OPEN);
		fixedClaw.setPosition(Constants.FIXED_CLAW_OPEN);
		susServo1.setPosition(Constants.SUS_SERVO1_UNLOCKED);
		susServo2.setPosition(Constants.SUS_SERVO2_UNLOCKED);

		telemetry.addLine("Select alliance (X - Blue, B - Red)");
		telemetry.update();

		alliance = Utilities.Alliance.BLUE; // TODO: detection

		while (!isStopRequested() && opModeInInit() && alliance == null)
		{
			if (driveInput.isPressed(new InputSystem.Key("x")) || armInput.isPressed(new InputSystem.Key("x")))
				alliance = Utilities.Alliance.BLUE;
			else if (driveInput.isPressed(new InputSystem.Key("b")) || armInput.isPressed(new InputSystem.Key("b")))
				alliance = Utilities.Alliance.RED;
			sleep(5);
		}

		telemetry.clear();
		telemetry.update();

		//	processor = new ColorBlobLocatorProcessor.Builder()
		//			.setTargetColorRange(alliance == Utilities.Alliance.RED ? ColorRange.RED : ColorRange.BLUE)
		//			.setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
		//			.setRoi(ImageRegion.entireFrame())
		//			.setDrawContours(true)
		//			.setDilateSize(50)
		//			.setErodeSize(50)
		//			.build();
//		portal = new VisionPortal.Builder()
//				.addProcessor(processor)
		//			.setCameraResolution(new Size(640, 480))
		//			.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
		//			.build();

		//	while (portal.getCameraState() != VisionPortal.CameraState.STREAMING)
		//		sleep(10);

		//	portal.getCameraControl(GainControl.class).setGain(255);
		//	portal.getCameraControl(ExposureControl.class).setExposure(20, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void OnRun()
	{
		Suspender();
		if (suspending) return;
		Drive();
		if (suspendState == Utilities.State.BUSY) return;
		Extendo();
		Scorer();
	}

	private void Drive()
	{
		float speed = 0.7f;
		if (driveInput.isPressed(Keybindings.Drive.TURBO_KEY)) speed = 1.0f;
		else if (driveInput.isPressed(Keybindings.Drive.SUPPRESS_KEY)) speed = 0.4f;
		mecanumDrive.setDrivePowers(
				new PoseVelocity2d(new Vector2d(driveInput.getValue(Keybindings.Drive.DRIVE_Y),
						driveInput.getValue(Keybindings.Drive.DRIVE_X)).times(-speed),
						driveInput.getValue(Keybindings.Drive.DRIVE_ROT_L) - driveInput.getValue(Keybindings.Drive.DRIVE_ROT_R)
				)
		);
	}

	private Utilities.State extendoState = Utilities.State.IDLE;
	private Utilities.State scorerState = Utilities.State.IDLE;
	private boolean rotatorLocked = false;
	private boolean autoRotator = false;
	private boolean waitingToDrop = false;
	private boolean grabbedWall = false;

	private double rotatorPos = Constants.ROTATOR_BASE;
	private double extendo1Value = Constants.EXTENDO1_RETRACTED;
	private double extendo2Value = Constants.EXTENDO2_RETRACTED;
	private int liftLevel = 0;
	private boolean liftOverride = false;

	private void Extendo()
	{
		// Rotator control
		if (driveInput.wasPressedThisFrame(Keybindings.Drive.AUTO_ROTATOR_KEY)) autoRotator = !autoRotator;

		if (!rotatorLocked) {
			if(!autoRotator) {
				rotatorPos += (float) (driveInput.getValue(Keybindings.Drive.ROTATOR_ANGLE) / 500f);
				rotatorPos = com.acmerobotics.roadrunner.Math.clamp(rotatorPos, 0, 1);
			}
			//		else
			//			SampleDetection();
			rotatorServo.setPosition(rotatorPos);
		}

		if (extendoState == Utilities.State.BUSY)
		{
			if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_FULL_KEY)) {
				extendo1Value = FaneOp.Constants.EXTENDO1_EXTENDED;
				extendo2Value = FaneOp.Constants.EXTENDO2_EXTENDED;
			} else if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_HALF_KEY)) {
				extendo1Value = Math.abs(FaneOp.Constants.EXTENDO1_EXTENDED + FaneOp.Constants.EXTENDO1_RETRACTED) / 2f;
				extendo2Value = Math.abs(FaneOp.Constants.EXTENDO2_EXTENDED + FaneOp.Constants.EXTENDO2_RETRACTED) / 2f;
			} else if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_ZERO_KEY)) {
				extendo1Value = FaneOp.Constants.EXTENDO1_RETRACTED;
				extendo2Value = FaneOp.Constants.EXTENDO2_RETRACTED;
			}

			if (extendo1Value != extendoServo1.getPosition() || extendo2Value != extendoServo2.getPosition()) {
				extendoServo1.setPosition(extendo1Value);
				extendoServo2.setPosition(extendo2Value);
			}
		}

		// Grabbing logic
		if (driveInput.wasPressedThisFrame(Keybindings.Drive.GRAB_WALL_KEY))
		{
			if (!grabbedWall) // Grab
			{
				fixedClaw.setPosition(Constants.FIXED_CLAW_CLOSED);
				setTimeout(() -> {
					liftOverride = true;
					liftLevel = 3;
					lift.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
					grabbedWall = true;
				}, 300);
			}
			else // Cancel
			{
				fixedClaw.setPosition(Constants.FIXED_CLAW_OPEN);
				lift.setTargetPosition(Constants.LIFT_IDLE);
				grabbedWall = false;
			}
		}
		if (extendoState == Utilities.State.IDLE
				&& !grabbedWall
				&& driveInput.wasPressedThisFrame(Keybindings.Drive.GRAB_TRANSFER_KEY)
				|| driveInput.wasPressedThisFrame(Keybindings.Drive.GRAB_HOLD_KEY))
		{
			extendoState = Utilities.State.BUSY;
			rotatorLocked = false;

			tumblerServo.setPosition(Constants.TUMBLER_DETECT);
			smallTumbler.setPosition(Constants.SMALL_TUMBLER_DETECT);
		}
		else if (extendoState == Utilities.State.BUSY)
		{
			// Grab and transfer
			if (driveInput.wasPressedThisFrame(Keybindings.Drive.GRAB_TRANSFER_KEY))
			{
				rotatorLocked = true;

				scoreClaw.setPosition(Constants.SCORE_CLAW_OPEN);

				tumblerServo.setPosition(Constants.TUMBLER_PICKUP);
				smallTumbler.setPosition(Constants.SMALL_TUMBLER_PICKUP);
				setTimeout(() -> {
					clawServo.setPosition(Constants.CLAW_CLOSED);
					setTimeout(() -> {
						extendo1Value = Constants.EXTENDO1_RETRACTED;
						extendo2Value = Constants.EXTENDO2_RETRACTED;
						extendoServo1.setPosition(Constants.EXTENDO1_RETRACTED);
						extendoServo2.setPosition(Constants.EXTENDO2_RETRACTED);
						tumblerServo.setPosition(Constants.TUMBLER_DELIVER);
						smallTumbler.setPosition(Constants.SMALL_TUMBLER_DELIVER);
						rotatorServo.setPosition(Constants.ROTATOR_BASE);
						rotatorPos = Constants.ROTATOR_BASE;
						setTimeout(() -> {
							scoreClaw.setPosition(Constants.SCORE_CLAW_CLOSED);
							setTimeout(() -> {
								clawServo.setPosition(Constants.CLAW_OPEN);
								setTimeout(() -> {
									tumblerServo.setPosition(Constants.TUMBLER_IDLE);
									smallTumbler.setPosition(Constants.SMALL_TUMBLER_DETECT);

									extendoState = Utilities.State.IDLE;
								}, 200);
							}, 200);
						}, 600);
					}, 300);
				}, 300);
			}
			// Grab and hold
			else if (driveInput.wasPressedThisFrame(Keybindings.Drive.GRAB_HOLD_KEY))
			{
				// Grab
				if (!waitingToDrop)
				{
					rotatorLocked = true;

					tumblerServo.setPosition(Constants.TUMBLER_PICKUP);
					smallTumbler.setPosition(Constants.SMALL_TUMBLER_PICKUP);
					setTimeout(() -> {
						clawServo.setPosition(Constants.CLAW_CLOSED);
						setTimeout(() -> {
							extendo1Value = Constants.EXTENDO1_RETRACTED;
							extendo2Value = Constants.EXTENDO2_RETRACTED;
							extendoServo1.setPosition(Constants.EXTENDO1_RETRACTED);
							extendoServo2.setPosition(Constants.EXTENDO2_RETRACTED);
							tumblerServo.setPosition(Constants.TUMBLER_DETECT);
							rotatorServo.setPosition(Constants.ROTATOR_BASE);
							rotatorPos = Constants.ROTATOR_BASE;
						}, 300);
					}, 300);
				}
				else // Release
				{
					clawServo.setPosition(Constants.CLAW_OPEN);
					setTimeout(() -> {
						tumblerServo.setPosition(Constants.TUMBLER_IDLE);
						smallTumbler.setPosition(Constants.SMALL_TUMBLER_DETECT);

						extendo1Value = Constants.EXTENDO1_RETRACTED;
						extendo2Value = Constants.EXTENDO2_RETRACTED;
						extendoServo1.setPosition(Constants.EXTENDO1_RETRACTED);
						extendoServo2.setPosition(Constants.EXTENDO2_RETRACTED);

						extendoState = Utilities.State.IDLE;
					}, 300);
				}
			}
		}
	}

	private void Scorer()
	{
		// Lift control logic
		if (!liftOverride)
		{
			// Lift level
			if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_LOW_KEY))
			{
				liftLevel = 0;
				scoreTumbler.setPosition(Constants.SCORE_TUMBLER_BASKET);
			}
			else if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_HIGH_KEY))
			{
				liftLevel = 1;
				scoreTumbler.setPosition(Constants.SCORE_TUMBLER_BASKET);
			}
			else if (armInput.wasPressedThisFrame(Keybindings.Arm.CHAMBER_HIGH_KEY))
			{
				liftLevel = 2;
				scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
			}

			// Lift central control
			if (scorerState == Utilities.State.BUSY)
				lift.setTargetPosition(FaneOp.Constants.LIFT_LEVELS[liftLevel]);
			else
				lift.setTargetPosition(FaneOp.Constants.LIFT_IDLE);
		}

		// Scoring logic
		if (armInput.wasPressedThisFrame(Keybindings.Arm.PRIMARY_KEY))
		{
			if (scorerState == Utilities.State.IDLE)
			{
				scorerState = Utilities.State.BUSY;
				if (grabbedWall) liftOverride = false;
				setTimeout(() -> scoreTumbler.setPosition(Constants.SCORE_TUMBLER_BASKET), 500);
			}
			else
			{
				if (liftLevel < 2) // Basket
				{
					scoreClaw.setPosition(Constants.SCORE_CLAW_OPEN);
					setTimeout(() -> {
						scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
						setTimeout(() -> scorerState = Utilities.State.IDLE, 500);
					}, 300);
				}
				else // Chamber
				{
					liftOverride = true;
					lift.setTargetPosition(Constants.LIFT_LEVELS[liftLevel] - Constants.LIFT_OFFSET);
					setTimeout(() -> {
						while (Math.abs(lift.getCurrentPosition() - lift.getTargetPosition()) > lift.getTargetPositionTolerance())
							sleep(15);
						fixedClaw.setPosition(Constants.FIXED_CLAW_OPEN);
						setTimeout(() -> {
							scorerState = Utilities.State.IDLE;
							liftOverride = false;
						}, 300);
					}, 500);
				}
			}
		}
	}

	private Utilities.State suspendState = Utilities.State.IDLE;
	private boolean suspending = false;

	private void Suspender()
	{
		if (armInput.wasPressedThisFrame(Keybindings.Arm.CANCEL_SUSPEND_KEY) && suspendState == Utilities.State.BUSY) {
			lift.setTargetPosition(Constants.LIFT_IDLE);
			suspendState = Utilities.State.IDLE;
			return;
		}

		if (!armInput.wasPressedThisFrame(Keybindings.Arm.SUSPEND_KEY)) return;

		if (suspendState == Utilities.State.IDLE) {
			suspendState = Utilities.State.BUSY;
			lift.setTargetPosition(Constants.LIFT_SUSPEND);
		} else {
			suspending = true;
			lift.setTargetPosition(Constants.LIFT_IDLE);

			susServo1.setPosition(Constants.SUS_SERVO1_LOCKED);
			susServo2.setPosition(Constants.SUS_SERVO2_LOCKED);

			sleep(300);

			mecanumDrive.leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
			mecanumDrive.rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
			mecanumDrive.leftBack.setPower(1);
			mecanumDrive.rightBack.setPower(1);
		}
	}

	private void SampleDetection() // Compute rotator angle
	{
		if (rotatorLocked || !autoRotator)
			return;

		List<ColorBlobLocatorProcessor.Blob> blobs = processor.getBlobs();
		ColorBlobLocatorProcessor.Util.filterByArea(1500, 200000, blobs);

		if (blobs.isEmpty())
			return;

		// Find biggest blob
		ColorBlobLocatorProcessor.Blob blob = blobs.get(0);
		for (ColorBlobLocatorProcessor.Blob b : blobs)
		{
			if (b.getContourArea() > blob.getContourArea())
				blob = b;
		}

		double angle = blob.getBoxFit().angle;
		org.opencv.core.Size size = blob.getBoxFit().size;

		// Math the angle
		if (size.width > size.height)
			angle += 90.0;

		// Command servo
		if (angle > 100)
			rotatorPos -= 0.00005 * (Math.abs(angle - 90));
		else if (angle < 80)
			rotatorPos += 0.00005 * (Math.abs(angle - 90));

		rotatorPos = com.acmerobotics.roadrunner.Math.clamp(rotatorPos, 0.0, 1.0);
	}

	@Override
	protected void OnTelemetry(Telemetry telemetry)
	{
		if (suspendState == Utilities.State.BUSY) {
			telemetry.addLine("[INFO] In Suspend Mode");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.SUSPEND_KEY + " again to suspend");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.CANCEL_SUSPEND_KEY + " to cancel");
		} else {
			telemetry.addLine("[INFO] Welcome Drivers!");
			telemetry.addData("[INFO] Auto rotator", autoRotator ? "Enabled" : "Disabled");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.SUSPEND_KEY + " to enter Suspend Mode");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.BASKET_LOW_KEY + " to move to Basket Low");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.BASKET_HIGH_KEY + " to move to Basket High");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.CHAMBER_HIGH_KEY + " to move to Submersible High");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.PRIMARY_KEY + " to score");
			telemetry.addLine("[INFO] Press " + Keybindings.Drive.GRAB_TRANSFER_KEY + " to grab");
			telemetry.addLine("[INFO] Press " + Keybindings.Drive.AUTO_ROTATOR_KEY + " to toggle auto-rotator");
		}
	}
}