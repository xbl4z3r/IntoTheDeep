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
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;

import java.util.List;

@TeleOp(name = " \uD83D\uDC79Fane\uD83D\uDC79 Op ", group = "TeleOp")
public final class FaneOp extends BaseOpMode
{
	private InputSystem driveInput, armInput;
	private MecanumDrive mecanumDrive;
	private DcMotorEx lift;
	private Servo extendoServo1, extendoServo2, tumblerServo, smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw;

	Utilities.Alliance alliance;

	ColorBlobLocatorProcessor processor;
	VisionPortal portal;

	private static class Constants
	{
		public static float EXTENDO1_RETRACTED = 0;
		public static float EXTENDO2_RETRACTED = 1;
		public static float EXTENDO1_EXTENDED = 1;
		public static float EXTENDO2_EXTENDED = 0;

		public static float TUMBLER_DELIVER = 0f;
		public static float TUMBLER_IDLE = 0.3f;
		public static float TUMBLER_DETECT = 0.715f;
		public static float TUMBLER_PICKUP = 0.99f;

		public static float SMALL_TUMBLER_DELIVER = 1f;
		public static float SMALL_TUMBLER_DETECT = 0f;
		public static float SMALL_TUMBLER_PICKUP = 0.25f;

		public static float ROTATOR_BASE = 0.5f;

		public static float CLAW_OPEN = 0.5f;
		public static float CLAW_CLOSED = 0f;

		public static int LIFT_IDLE = 0;
		public static int LIFT_SUSPEND = 0;
		public static int LIFT_OFFSET = 250;

		// BASKET_LOW, BASKET_HIGH, SUBMERSIBLE_HIGH
		public static int[] LIFT_LEVELS = { 330, 550, 1100 };

		public static float SCORE_TUMBLER_IDLE = 0;
		public static float SCORE_TUMBLER_BUSY = 0.75f;

		public static float SCORE_CLAW_IDLE = 1;
		public static float SCORE_CLAW_BUSY = 0;
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
			public static final InputSystem.Key EXTENDO_FULL_KEY = new InputSystem.Key("dpad_up");
			public static final InputSystem.Key EXTENDO_HALF_KEY = new InputSystem.Key("dpad_left");
			public static final InputSystem.Key EXTENDO_ZERO_KEY = new InputSystem.Key("dpad_down");
			public static final InputSystem.Key TURBO_KEY = new InputSystem.Key("right_bumper");
			public static final InputSystem.Key SUPPRESS_KEY = new InputSystem.Key("left_bumper");
			public static final InputSystem.Axis DRIVE_X = new InputSystem.Axis("left_stick_x");
			public static final InputSystem.Axis DRIVE_Y = new InputSystem.Axis("left_stick_y");
			public static final InputSystem.Axis DRIVE_ROT_L = new InputSystem.Axis("left_trigger");
			public static final InputSystem.Axis DRIVE_ROT_R = new InputSystem.Axis("right_trigger");
			public static final InputSystem.Axis ROTATOR_ANGLE = new InputSystem.Axis("right_stick_x");
			public static final InputSystem.Key PRIMARY_KEY = new InputSystem.Key("a");
			public static final InputSystem.Key AUTO_ROTATOR_KEY = new InputSystem.Key("x");
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

		extendoServo1.setPosition(Constants.EXTENDO1_RETRACTED);
		extendoServo2.setPosition(Constants.EXTENDO2_RETRACTED);
		tumblerServo.setPosition(Constants.TUMBLER_IDLE);
		smallTumbler.setPosition(Constants.SMALL_TUMBLER_DELIVER);
		rotatorServo.setPosition(Constants.ROTATOR_BASE);
		clawServo.setPosition(Constants.CLAW_OPEN);
		scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
		scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE);

		telemetry.addLine("Select alliance (X - Blue, B - Red)");
		telemetry.update();

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

		processor = new ColorBlobLocatorProcessor.Builder()
				.setTargetColorRange(alliance == Utilities.Alliance.RED ? ColorRange.RED : ColorRange.BLUE)
				.setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
				.setRoi(ImageRegion.entireFrame())
				.setDrawContours(true)
				.setDilateSize(50)
				.setErodeSize(50)
				.build();
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
						driveInput.getValue(Keybindings.Drive.DRIVE_X)).times(speed),
						-driveInput.getValue(Keybindings.Drive.DRIVE_ROT_L) + driveInput.getValue(Keybindings.Drive.DRIVE_ROT_R)
				)
		);
	}

	private Utilities.State extendoState = Utilities.State.IDLE;
	private Utilities.State scorerState = Utilities.State.IDLE;
	private boolean loaded = false;
	private boolean rotatorLocked = false;
	private boolean autoRotator = false;
	private boolean manualRotatorOverride = false;

	private double rotatorValue = Constants.ROTATOR_BASE;
	private double extendo1Value = Constants.EXTENDO1_RETRACTED;
	private double extendo2Value = Constants.EXTENDO2_RETRACTED;
	private int liftLevel = 0;

	/*
	 *                                         ┌──────────────────┐
	 *                                         │scorerState: IDLE │
	 *                                         │                  │
	 *         ┌───────┐                       │extendoState: IDLE│
	 *         │STATE  │                  ┌────┤                  │
	 *         │MACHINE│                  │    │rotator: LOCKED   │
	 *         └───────┘                  │    │                  │
	 *                                    │    │loaded: FALSE     │
	 *                                    │    └──────────────────┘   ┌──────────────────┐
	 *                                    │                           │scorerState: IDLE │
	 *                                 ┌──┴─┐        A                │                  │
	 *                            ┌───►│Idle├─────────────────┐       │extendoState: BUSY│
	 *                            │    └────┘                 │       │                  │
	 *                            │       ▲                   ▼       │rotator: UNLOCKED │
	 *                            │       │               ┌────────┐  │                  │
	 *             A (Arm Gamepad)│       │               │Extended├──┤loaded: FALSE     │
	 *                            │       │               └───┬────┘  └──────────────────┘
	 *                            │       │                   │
	 * ┌──────────────────┐       │       │                   │A      ┌──────────────────┐
	 * │scorerState: BUSY │       │       │       FAIL        │       │scorerState: IDLE │
	 * │                  │       │       └───────────────────┤       │                  │
	 * │extendoState: IDLE│       │                           │SUCCESS│extendoState: IDLE│
	 * │                  │       │                           │       │                  │
	 * │rotator: LOCKED   │       │                           ▼       │rotator: LOCKED   │
	 * │                  │   ┌───┴───┐    A (Arm Gamepad) ┌──────┐   │                  │
	 * │loaded: TRUE      ├───┤Scoring│◄───────────────────┤Loaded├───┤loaded: TRUE      │
	 * └──────────────────┘   └───────┘                    └──────┘   └──────────────────┘
	 *
	 */
	private void Extendo()
	{
		if (driveInput.wasPressedThisFrame(Keybindings.Drive.AUTO_ROTATOR_KEY)) autoRotator = !autoRotator;

		if (!rotatorLocked) {
			if(!autoRotator) {
				rotatorValue += (float) (driveInput.getValue(Keybindings.Drive.ROTATOR_ANGLE) / 10f);
				rotatorValue = Utilities.Clamp(rotatorValue, 0, 1);
			}
	//		else
	//			SampleDetection();
			rotatorServo.setPosition(rotatorValue);
		}

		if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_FULL_KEY)) {
			extendo1Value = Constants.EXTENDO1_EXTENDED;
			extendo2Value = Constants.EXTENDO2_EXTENDED;
		} else if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_HALF_KEY)) {
			extendo1Value = Math.abs(Constants.EXTENDO1_EXTENDED - Constants.EXTENDO1_RETRACTED) / 2f;
			extendo2Value = Math.abs(Constants.EXTENDO2_EXTENDED - Constants.EXTENDO2_RETRACTED) / 2f;
		} else if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_ZERO_KEY)) {
			extendo1Value = Constants.EXTENDO1_RETRACTED;
			extendo2Value = Constants.EXTENDO2_RETRACTED;
		}

		if (extendoState == Utilities.State.BUSY &&
				extendo1Value != extendoServo1.getPosition() &&
				extendo2Value != extendoServo2.getPosition()) {
			extendoServo1.setPosition(extendo1Value);
			extendoServo2.setPosition(extendo2Value);
		}

		// Grab
		if (!driveInput.wasPressedThisFrame(Keybindings.Drive.PRIMARY_KEY)) return;
		if (extendoState == Utilities.State.IDLE) {
			extendoState = Utilities.State.BUSY;
			rotatorLocked = false;

			if (scorerState == Utilities.State.IDLE)
				loaded = false; // If the scorer is not busy and the extendo is busy, the claw is not loaded

			extendoServo1.setPosition(extendo1Value);
			extendoServo2.setPosition(extendo2Value);
			tumblerServo.setPosition(Constants.TUMBLER_DETECT);
			smallTumbler.setPosition(Constants.SMALL_TUMBLER_DETECT);
		} else if (extendoState == Utilities.State.BUSY) {
			rotatorLocked = true;

			scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE); // In case the claw is closed
			tumblerServo.setPosition(Constants.TUMBLER_PICKUP);
			smallTumbler.setPosition(Constants.SMALL_TUMBLER_PICKUP);
			setTimeout(() -> {
				clawServo.setPosition(Constants.CLAW_CLOSED);
				setTimeout(() -> {
					extendoServo1.setPosition(Constants.EXTENDO1_RETRACTED);
					extendoServo2.setPosition(Constants.EXTENDO2_RETRACTED);
					extendo1Value = Constants.EXTENDO1_RETRACTED;
					extendo2Value = Constants.EXTENDO2_RETRACTED;
					tumblerServo.setPosition(Constants.TUMBLER_DELIVER);
					smallTumbler.setPosition(Constants.SMALL_TUMBLER_DELIVER);
					rotatorServo.setPosition(Constants.ROTATOR_BASE);
					rotatorValue = Constants.ROTATOR_BASE;
					setTimeout(() -> {
						scoreClaw.setPosition(Constants.SCORE_CLAW_BUSY);
						setTimeout(() -> {
							clawServo.setPosition(Constants.CLAW_OPEN);

							setTimeout(() -> tumblerServo.setPosition(Constants.TUMBLER_IDLE), 200);

							extendoState = Utilities.State.IDLE;
							loaded = true; // If the extendo is idle, the claw should be loaded
						}, 300);
					}, 500);
				}, 500);
			}, 400);
		}
	}

	private void Scorer()
	{
		if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_LOW_KEY)) liftLevel = 0;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_HIGH_KEY)) liftLevel = 1;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.CHAMBER_HIGH_KEY)) liftLevel = 2;

		if (scorerState == Utilities.State.BUSY)
			lift.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);

		if (!loaded) return;
		if (!armInput.wasPressedThisFrame(Keybindings.Arm.PRIMARY_KEY)) return;
		if (scorerState == Utilities.State.IDLE) {
			scorerState = Utilities.State.BUSY;

			lift.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			scoreClaw.setPosition(Constants.SCORE_CLAW_BUSY);
			setTimeout(() -> scoreTumbler.setPosition(Constants.SCORE_TUMBLER_BUSY), 500 + 250 * (liftLevel >= 1 ? 1 : 0));
		} else if (scorerState == Utilities.State.BUSY) {
			if (liftLevel < 2) { // BASKET_LOW, BASKET_HIGH
				scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE);
				setTimeout(() -> {
					scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
					setTimeout(() -> {
						lift.setTargetPosition(Constants.LIFT_IDLE);

						scorerState = Utilities.State.IDLE;
						loaded = false;
					}, 1000);
				}, 500);
			} else { // SUBMERSIBLE_HIGH
				lift.setTargetPosition(Constants.LIFT_LEVELS[liftLevel] - Constants.LIFT_OFFSET);
				setTimeout(() -> {
					scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE);
					setTimeout(() -> {
						scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
						setTimeout(() -> {
							lift.setTargetPosition(Constants.LIFT_IDLE);

							scorerState = Utilities.State.IDLE;
							loaded = false;
						}, 500);
					}, 500);
				}, 200);
			}
		}
	}

	private Utilities.State suspendState = Utilities.State.IDLE;

	// TODO: Lock the axle in place when suspending the arm and use the drive motors to power the lift
	private void Suspender()
	{
		if (armInput.wasPressedThisFrame(Keybindings.Arm.CANCEL_SUSPEND_KEY) && suspendState == Utilities.State.BUSY) {
			lift.setTargetPosition(Constants.LIFT_IDLE);
			suspendState = Utilities.State.IDLE;
			return;
		}

		if (!armInput.wasPressedThisFrame(Keybindings.Arm.SUSPEND_KEY)) return;

		if (suspendState == Utilities.State.IDLE) {
			lift.setTargetPosition(Constants.LIFT_SUSPEND);
			suspendState = Utilities.State.BUSY;
		} else {
			lift.setTargetPosition(Constants.LIFT_IDLE);
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
			rotatorValue -= 0.00005 * (Math.abs(angle - 90));
		else if (angle < 80)
			rotatorValue += 0.00005 * (Math.abs(angle - 90));

		rotatorValue = Utilities.Clamp(rotatorValue, 0.0, 1.0);

	//	if ((servoPos == 0 && angle > 100) || (servoPos == 1 && angle < 80))
	//	{
	//		servoPos = 0.5;
	//		servo.setPosition(servoPos);
	//		sleep(300);
	//	}

	//	telemetry.addData("Width:       ", size.width);
	//	telemetry.addData("Height:      ", size.height);
	//	telemetry.addData("Angle:       ", angle);
	//	telemetry.addData("Area:        ", blob.getContourArea());
	}

	@Override
	protected void OnTelemetry(Telemetry telemetry)
	{
		telemetry.addData("Alliance: ", alliance.name());
	//	telemetry.addData("FPS:  ", portal.getFps());
		telemetry.addData("Rotator Pos: ", rotatorValue);

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
			telemetry.addLine("[INFO] Press " + Keybindings.Drive.PRIMARY_KEY + " to grab");
			telemetry.addLine("[INFO] Press " + Keybindings.Drive.AUTO_ROTATOR_KEY + " to toggle auto-rotator");
		}
	}
}