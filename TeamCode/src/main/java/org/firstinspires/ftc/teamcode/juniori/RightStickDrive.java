package org.firstinspires.ftc.teamcode.juniori;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="Right Stick Drive", group="TeleOp")
public class RightStickDrive extends LinearOpMode {

	// Define motor variables
	private DcMotor frontLeftMotor;
	private DcMotor frontRightMotor;
	private DcMotor backLeftMotor;
	private DcMotor backRightMotor;

	@Override
	public void runOpMode() {
		// Initialize the motors from the hardware map
		frontLeftMotor = hardwareMap.get(DcMotor.class, "frontLeftMotor");
		frontRightMotor = hardwareMap.get(DcMotor.class, "frontRightMotor");
		backLeftMotor = hardwareMap.get(DcMotor.class, "backLeftMotor");
		backRightMotor = hardwareMap.get(DcMotor.class, "backRightMotor");

		// Set the direction of the motors if needed
		frontLeftMotor.setDirection(DcMotor.Direction.FORWARD);
		frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
		backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
		backRightMotor.setDirection(DcMotor.Direction.REVERSE);

		// Wait for the driver to press "play"
		waitForStart();

		// Main control loop with while and !isStopRequested()
		while (opModeIsActive() && !isStopRequested()) {
			// Get right stick x and y values (y is inverted)
			double rightStickX = gamepad1.right_stick_x;
			double rightStickY = -gamepad1.right_stick_y; // Y is inverted on gamepad

			// Calculate power for each motor
			double frontLeftPower = rightStickY + rightStickX;
			double frontRightPower = rightStickY - rightStickX;
			double backLeftPower = rightStickY + rightStickX;
			double backRightPower = rightStickY - rightStickX;

			// Normalize the values so no motor power exceeds 1.0
			double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
			max = Math.max(max, Math.abs(backLeftPower));
			max = Math.max(max, Math.abs(backRightPower));

			if (max > 1.0) {
				frontLeftPower /= max;
				frontRightPower /= max;
				backLeftPower /= max;
				backRightPower /= max;
			}

			// Set the motor powers
			frontLeftMotor.setPower(frontLeftPower);
			frontRightMotor.setPower(frontRightPower);
			backLeftMotor.setPower(backLeftPower);
			backRightMotor.setPower(backRightPower);

			// Call telemetry function
			sendTelemetry(frontLeftPower, frontRightPower, backLeftPower, backRightPower);
		}
	}

	// Separate function for telemetry
	private void sendTelemetry(double frontLeftPower, double frontRightPower, double backLeftPower, double backRightPower) {
		telemetry.addData("Front Left Power", frontLeftPower);
		telemetry.addData("Front Right Power", frontRightPower);
		telemetry.addData("Back Left Power", backLeftPower);
		telemetry.addData("Back Right Power", backRightPower);
		telemetry.update();
	}
}