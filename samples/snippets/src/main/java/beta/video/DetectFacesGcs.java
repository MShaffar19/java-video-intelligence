/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package beta.video;

// [START video_detect_faces_gcs_beta]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1p3beta1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1p3beta1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1p3beta1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1p3beta1.DetectedAttribute;
import com.google.cloud.videointelligence.v1p3beta1.FaceDetectionAnnotation;
import com.google.cloud.videointelligence.v1p3beta1.FaceDetectionConfig;
import com.google.cloud.videointelligence.v1p3beta1.Feature;
import com.google.cloud.videointelligence.v1p3beta1.TimestampedObject;
import com.google.cloud.videointelligence.v1p3beta1.Track;
import com.google.cloud.videointelligence.v1p3beta1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1p3beta1.VideoContext;
import com.google.cloud.videointelligence.v1p3beta1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1p3beta1.VideoSegment;

public class DetectFacesGcs {

  public static void detectFacesGcs() throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String gcsUri = "gs://cloud-samples-data/video/googlework_short.mp4";
    detectFacesGcs(gcsUri);
  }

  // Detects faces in a video stored in Google Cloud Storage using the Cloud Video Intelligence API.
  public static void detectFacesGcs(String gcsUri) throws Exception {
    try (VideoIntelligenceServiceClient videoIntelligenceServiceClient =
        VideoIntelligenceServiceClient.create()) {

      FaceDetectionConfig faceDetectionConfig =
          FaceDetectionConfig.newBuilder()
              // Must set includeBoundingBoxes to true to get facial attributes.
              .setIncludeBoundingBoxes(true)
              .setIncludeAttributes(true)
              .build();
      VideoContext videoContext =
          VideoContext.newBuilder().setFaceDetectionConfig(faceDetectionConfig).build();

      AnnotateVideoRequest request =
          AnnotateVideoRequest.newBuilder()
              .setInputUri(gcsUri)
              .addFeatures(Feature.FACE_DETECTION)
              .setVideoContext(videoContext)
              .build();

      // Detects faces in a video
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> future =
          videoIntelligenceServiceClient.annotateVideoAsync(request);

      System.out.println("Waiting for operation to complete...");
      AnnotateVideoResponse response = future.get();

      // Gets annotations for video
      VideoAnnotationResults annotationResult = response.getAnnotationResultsList().get(0);

      // Annotations for list of people detected, tracked and recognized in video.
      for (FaceDetectionAnnotation faceDetectionAnnotation :
          annotationResult.getFaceDetectionAnnotationsList()) {
        System.out.print("Face detected:\n");
        for (Track track : faceDetectionAnnotation.getTracksList()) {
          VideoSegment segment = track.getSegment();
          System.out.printf(
              "\tStart: %d.%.0fs\n",
              segment.getStartTimeOffset().getSeconds(),
              segment.getStartTimeOffset().getNanos() / 1e6);
          System.out.printf(
              "\tEnd: %d.%.0fs\n",
              segment.getEndTimeOffset().getSeconds(), segment.getEndTimeOffset().getNanos() / 1e6);

          // Each segment includes timestamped objects that
          // include characteristics of the face detected.
          TimestampedObject firstTimestampedObject = track.getTimestampedObjects(0);

          for (DetectedAttribute attribute : firstTimestampedObject.getAttributesList()) {
            // Attributes include unique pieces of clothing, like glasses,
            // poses, or hair color.
            System.out.printf("\tAttribute: %s;\n", attribute.getName());
          }
        }
      }
    }
  }
}
// [END video_detect_faces_gcs_beta]
