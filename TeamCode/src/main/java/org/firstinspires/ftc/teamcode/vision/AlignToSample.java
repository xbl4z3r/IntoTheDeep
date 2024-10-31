package org.firstinspires.ftc.teamcode.vision;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Utilities;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;

import java.util.List;

@TeleOp(name = "Align To Sample Test", group = "Vision")
public class AlignToSample extends LinearOpMode
{
	Utilities.Alliance alliance;

	ColorBlobLocatorProcessor processor;
	VisionPortal portal;

	Servo servo;

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

			if (gamepad1.x)
				alliance = Utilities.Alliance.BLUE;
			else if (gamepad1.b)
				alliance = Utilities.Alliance.RED;
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


		servo = hardwareMap.get(Servo.class, "rotator");
		servo.setPosition(0.5);
	}

	private void Run()
	{
		while (!isStopRequested())
		{
			List<ColorBlobLocatorProcessor.Blob> blobs = processor.getBlobs();
			ColorBlobLocatorProcessor.Util.filterByArea(1500, 200000, blobs);

			telemetry.update();
			telemetry.addData("FPS:         ", portal.getFps());
			telemetry.addData("Servo Angle: ", servo.getPosition());

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
			servo.setPosition(1.0 - (angle / 180.0));

			// Telemetry
			telemetry.addData("Width:       ", size.width);
			telemetry.addData("Height:      ", size.height);
			telemetry.addData("Angle:       ", angle);
			telemetry.addData("Area:        ", blob.getContourArea());
		}
	}
}