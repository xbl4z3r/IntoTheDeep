package org.firstinspires.ftc.teamcode.vision;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.teamcode.Utilities;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;

import java.util.List;
import java.util.concurrent.TimeUnit;

@TeleOp(name = "Align To Sample Test", group = "Vision")
public class AlignToSample extends LinearOpMode
{
	Utilities.Alliance alliance;

	ColorBlobLocatorProcessor processor;
	VisionPortal portal;

	Servo servo;
	double servoPos = 0.5;

	@Override
	public void runOpMode() throws InterruptedException
	{
		Init();
		//waitForStart();
		Run();
	}

	private void Init()
	{
		telemetry.setMsTransmissionInterval(50);
		telemetry.addLine("Select alliance (X - Blue, B - Red)");
		telemetry.update();

		while (alliance == null && !isStopRequested())
		{
			alliance = Utilities.Alliance.RED;
			if (gamepad1.x || gamepad2.x)
				alliance = Utilities.Alliance.BLUE;
			else if (gamepad1.b || gamepad2.b)
				alliance = Utilities.Alliance.RED;
			sleep(5);
		}

		telemetry.clear();

		processor = new ColorBlobLocatorProcessor.Builder()
				.setTargetColorRange(alliance == Utilities.Alliance.RED ? ColorRange.RED : ColorRange.BLUE)
				.setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
				.setRoi(ImageRegion.entireFrame())
				.setDrawContours(true)
				.setDilateSize(50)
				.setErodeSize(50)
				.build();
		portal = new VisionPortal.Builder()
				.addProcessor(processor)
				.setCameraResolution(new Size(640, 480))
				.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
				.build();

		while (portal.getCameraState() != VisionPortal.CameraState.STREAMING)
			sleep(10);

		portal.getCameraControl(GainControl.class).setGain(255);
		portal.getCameraControl(ExposureControl.class).setExposure(20, TimeUnit.MILLISECONDS);

		servo = hardwareMap.get(Servo.class, "rotator");
		servo.setPosition(servoPos);
	}

	private void Run()
	{
		while (!isStopRequested())
		{
			List<ColorBlobLocatorProcessor.Blob> blobs = processor.getBlobs();
			ColorBlobLocatorProcessor.Util.filterByArea(1500, 200000, blobs);

			telemetry.update();
			telemetry.addData("FPS:         ", portal.getFps());
			telemetry.addData("Servo Angle: ", servoPos);

			if (blobs.isEmpty())
				continue;

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
				servoPos -= 0.00005 * (Math.abs(angle - 90));
			else if (angle < 80)
				servoPos += 0.00005 * (Math.abs(angle - 90));

			servoPos = com.acmerobotics.roadrunner.Math.clamp(servoPos, 0.0, 1.0);

			if ((servoPos == 0 && angle > 100) || (servoPos == 1 && angle < 80))
			{
				servoPos = 0.5;
				servo.setPosition(servoPos);
				sleep(300);
			}

			servo.setPosition(servoPos);

			// Telemetry
			telemetry.addData("Width:       ", size.width);
			telemetry.addData("Height:      ", size.height);
			telemetry.addData("Angle:       ", angle);
			telemetry.addData("Area:        ", blob.getContourArea());
		}
	}
}