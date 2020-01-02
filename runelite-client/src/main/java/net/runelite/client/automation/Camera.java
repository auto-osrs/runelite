/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.automation;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.client.automation.input.Keyboard;
import net.runelite.client.automation.input.KeyboardKey;
import net.runelite.client.automation.util.BotExecutor;
import net.runelite.client.automation.util.Execution;
import net.runelite.client.automation.util.calculations.BotMath;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Camera
{
	@Inject
	private static Client client;

	private static ExecutorService executor = BotExecutor.getExecutor();

	private static ExecutorService keyboardExecutor = Executors.newFixedThreadPool(2);

	/**
	 * The camera yaw tolerance to use when calculating whether to change the
	 * yaw value (1.5%).
	 */
	private static final double CAMERA_YAW_TOLERANCE = 0.015;

	/**
	 * The camera pitch tolerance to use when calculating whether to change
	 * the pitch value (3.5%).
	 */
	private static final double CAMERA_PITCH_TOLERANCE = 0.035;

	/**
	 * Gets the current yaw value as an angle.
	 *
	 * @return The camera yaw as an angle, this is an angle between 0-360.
	 */
	public static int getYaw() {
		return (int) (client.getCameraYaw() / Constants.MAX_CAMERA_YAW * 360);
	}

	/**
	 * Gets the current pitch value normalised to be between 0 and 1.
	 *
	 * @return The normalised camera pitch.
	 */
	public static double getPitch() {
		double normalisedCameraPitch = BotMath.normalise(
				client.getCameraPitch(),
				Constants.MIN_CAMERA_PITCH,
				Constants.MAX_CAMERA_PITCH
		);

		return BotMath.round(normalisedCameraPitch, 3);
	}

	/**
	 * Turns the camera to the specified yaw.
	 *
	 * @param yaw The yaw angle to turn to, this should be an angle between 0 and 360.
	 */
	public static void turnTo(int yaw) {
		turnTo(yaw, getPitch());
	}

	/**
	 * Turns the camera to the specified yaw.
	 *
	 * @param pitch The normalised pitch to turn to, this should be a value between 0 and 1.
	 */
	public static void turnTo(double pitch) {
		turnTo(getYaw(), pitch);
	}

	/**
	 * Turns the camera to the specified yaw using a set tolerance.
	 *
	 * @param yaw The yaw angle to turn to, this should be an angle between 0 and 360.
	 * @param pitch The normalised pitch to turn to, this should be between 0 and 1.
	 */
	public static void turnTo(int yaw, double pitch) {

		if (yaw < 0 || yaw > 360) {
			log.info(String.format("[CAMERA] Your target yaw (%d) must be between 0 and 360.", yaw));
			return;
		}

		if (pitch < 0 || pitch > 1) {
			log.info(String.format("[CAMERA] Your target pitch (%.2f) must be between 0 and 1.", pitch));
			return;
		}

		executor.submit(() -> {
			keyboardExecutor.submit(() -> {
				processPitchMovement(getPitch(), pitch);
			});

			keyboardExecutor.submit(() -> {
				processYawMovement(getYaw(), yaw);
			});
		});
	}

	/**
	 * Processes the yaw movement (if any) using the relevant keyboard keys.
	 *
	 * @param currentYaw The current camera yaw angle.
	 * @param targetYaw The target camera yaw angle.
	 */
	private static void processYawMovement(int currentYaw, int targetYaw) {
		boolean shouldTurnLeft = shouldTurnLeft(currentYaw, targetYaw);
		boolean needsToChangeYaw = getAngleDifference(currentYaw, targetYaw) > (360 * CAMERA_YAW_TOLERANCE);

		KeyboardKey yawKey = shouldTurnLeft
				? KeyboardKey.KEY_D
				: KeyboardKey.KEY_A;

		if (needsToChangeYaw) {
			log.info(
					String.format("[CAMERA] Changing yaw to %d, turning %s by pressing %s. (Current Yaw: %d)",
							targetYaw,
							shouldTurnLeft
									? "left"
									: "right",
							yawKey.toString(),
							currentYaw
					)
			);

			Keyboard.pressKey(yawKey);
		}

		while (needsToChangeYaw) {
			needsToChangeYaw = getAngleDifference(getYaw(), targetYaw) > (360 * CAMERA_YAW_TOLERANCE);

			if (!needsToChangeYaw) {
				log.info(
						String.format("[CAMERA] Reached yaw of %d, releasing %s. (Target Yaw: %d).",
								getYaw(), yawKey.toString(), targetYaw
						)
				);

				Keyboard.releaseKey(yawKey);
			}

			Execution.delay(15, 10, 20);
		}
	}

	/**
	 * Performs the pitch movement (if any) using the relevant keyboard keys.
	 *
	 * @param currentPitch The current normalised camera pitch.
	 * @param targetPitch The target normalised camera pitch.
	 */
	private static void processPitchMovement(double currentPitch, double targetPitch) {
		boolean needsToChangePitch;
		boolean shouldMoveUp = targetPitch > currentPitch;
		double pitchDifference = currentPitch - targetPitch;

		// Check whether the pitchDifference is large enough to justify moving the camera.
		if (Math.abs(pitchDifference) < CAMERA_PITCH_TOLERANCE) {
			needsToChangePitch = false;
		} else {
			// Calculate whether we have crossed the target pitch boundary.
			needsToChangePitch = shouldMoveUp
					? pitchDifference < 0
					: pitchDifference > 0;
		}

		KeyboardKey pitchKey = shouldMoveUp
				? KeyboardKey.KEY_W
				: KeyboardKey.KEY_S;

		if (needsToChangePitch) {
			log.info(
					String.format("[CAMERA] Changing pitch to %.2f, moving %s by pressing %s. (Current Pitch: %.2f)",
							targetPitch,
							shouldMoveUp
									? "up"
									: "down",
							pitchKey.toString(),
							getPitch()
					)
			);

			Keyboard.pressKey(pitchKey);
		}

		while (needsToChangePitch) {
			pitchDifference = getPitch() - targetPitch;
			needsToChangePitch = shouldMoveUp
					? pitchDifference < 0
					: pitchDifference > 0;

			if (!needsToChangePitch) {
				log.info(
						String.format("[CAMERA] Reached pitch of %.2f, releasing %s. (Target Pitch: %.2f).",
								getPitch(), pitchKey.toString(), targetPitch
						)
				);

				Keyboard.releaseKey(pitchKey);
			}

			Execution.delay(15, 10, 20);
		}
	}

	/**
	 * Calculates whether the quickest route to the target yaw is to turn
	 * the camera left.
	 *
	 * @param startYaw The current camera yaw.
	 * @param targetYaw The yaw that we are aiming to turn to.
	 * @return True if the camera should turn left, or false if it should turn right.
	 */
	private static boolean shouldTurnLeft(int startYaw, int targetYaw) {
		return getAngleDifference(startYaw, targetYaw) < 180;
	}

	/**
	 * Calculates the normalised difference between 2 angles.
	 *
	 * @param startAngle This is the starting angle.
	 * @param targetAngle This is the target/end angle.
	 * @return The rotational difference between the 2 angles.
	 */
	private static int getAngleDifference(int startAngle, int targetAngle) {
		return (targetAngle - startAngle + 360) % 360;
	}
}
