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
			telemetry.addData("FPS: ", portal.getFps());

			if (blobs.isEmpty())
				continue;

			ColorBlobLocatorProcessor.Blob blob = blobs.get(0);
			for (ColorBlobLocatorProcessor.Blob b : blobs)
			{
				if (b.getContourArea() > blob.getContourArea())
					blob = b;
			}

			telemetry.addData("Angle", blob.getBoxFit().toString());
			telemetry.addData("Size", blob.getContourArea());
		}
	}
}