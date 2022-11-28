/*
 * Copyright 2022 University of Basel, Department of Mathematics and Computer Science
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
package data

import scala.util.Try
import scalismo.mesh.TriangleMesh
import scalismo.io.MeshIO
import java.io.File
import scalismo.geometry._3D
import scalismo.statisticalmodel.StatisticalMeshModel.apply
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.io.StatisticalModelIO
import scalismo.statisticalmodel.PointDistributionModel
import scalismo.transformations.Scaling3D
import com.jogamp.nativewindow.util.Point



/**
 * The data repository is used to access all the data that it
 * used in this case study.
 */
object DataRepository:

    private def dataDir : File = File("./data")
    private val meshDir : File = File(dataDir, ".")
    private val modelDir : File = File(dataDir, "model")

    /**
      * The ids of the datasets used in this case study
      */
    def ids : Seq[FaceId] = Seq(
        FaceId("00002_20061015_00448_neutral_face05"),
        FaceId("00006_20080430_04384_neutral_face05"),
        FaceId("00014_20080430_04338_neutral_face05"),
        FaceId("00017_20061201_00812_neutral_face05"),
        FaceId("00021_20061127_00715_neutral_face05"),
        FaceId("00052_20061024_00526_neutral_face05"),
        FaceId("00053_20061024_00542_neutral_face05"),
        FaceId("00293_20080104_03413_neutral_face05"),
        FaceId("00323_20080125_03764_neutral_face05"),
    )

    /**
      * returns the file corresponding to the given id
      */
    def faceMeshFile(id : FaceId) : File = File(meshDir, id.toString + ".ply")

    /**
      * Loads the face mesh for the given id    
      * As the original files downloaded from the Basel Face Model 
      * use micrometers as a unit, we scale all the meshes by a factor of 
      * 10^-3 to obtain milimeters.
      */
    def loadFaceMesh(id : FaceId) : Try[TriangleMesh[_3D]] =
        MeshIO.readMesh(faceMeshFile(id))
            .map(_.transform(Scaling3D(0.001))) // scale from micrometers to mm

    /**
     * The id of the reference mesh
     */
    def referenceId : FaceId = ids.head

    /**
      * Loads the refrence mesh
      */
    def loadReferenceMesh() : Try[TriangleMesh[_3D]] = loadFaceMesh(referenceId)


    /**
      * Loads the test mesh, for which we cut away the nose
      */
    def loadFaceMeshNoNose() : Try[TriangleMesh[_3D]] = 
        loadFaceMesh(FaceId("00001_20061015_00418_neutral_face05-no-nose"))

    /**
      * The file holding the GP-Model estimated from data
      */
    val faceModelFile : File = File(modelDir, "statisticalModel.h5")
    
    /**
      * Writes the given GP-Model 
      *
      */
    def writeFaceModel(model : PointDistributionModel[_3D, TriangleMesh]) : Try[Unit] = 
        if (!modelDir.exists()) modelDir.mkdirs()
        StatisticalModelIO.writeStatisticalTriangleMeshModel3D(model, faceModelFile)


    /** 
     * Reads the learned GP-Model
     */
    def loadFaceModel() : Try[PointDistributionModel[_3D, TriangleMesh]] = 
        StatisticalModelIO.readStatisticalTriangleMeshModel3D(faceModelFile)

    /**
     * The file holding an augmented GP model
     */
    val augmentedFaceModelFile : File = File(modelDir, "augmentedStatisticalModel.h5")

    /**
      * Writes the given augmented model to file
     */
    def writeAugmentedFaceModel(model : PointDistributionModel[_3D, TriangleMesh]) : Try[Unit] = 
        if (!modelDir.exists()) modelDir.mkdirs()
        StatisticalModelIO.writeStatisticalTriangleMeshModel3D(model, augmentedFaceModelFile)

    /**
     * Loads the augmented model
     */
    def loadAugmentedFaceModel() : Try[PointDistributionModel[_3D, TriangleMesh]] = 
        StatisticalModelIO.readStatisticalTriangleMeshModel3D(augmentedFaceModelFile)

    /** 
     * The file holding the posterior model for the nose
     */
    val posteriorModelFile : File = File(modelDir, "nosePosterior.h5")
    
    /**
      * Writes the posterior model
      */
    def writeNosePosteriorModel(model : PointDistributionModel[_3D, TriangleMesh]) : Try[Unit] =
      StatisticalModelIO.writeStatisticalTriangleMeshModel3D(model, posteriorModelFile) 

    /**
      * Loads the nose posterior model
      */
    def loadNosePosteriorModel() : Try[PointDistributionModel[_3D, TriangleMesh]] = 
      StatisticalModelIO.readStatisticalTriangleMeshModel3D(posteriorModelFile)
    

/**
 * Each face is identified by a unique id. This type represents is used to 
 * represent this id.
 */
opaque type FaceId = String
object FaceId:
    def apply(id : String) : FaceId = new FaceId(id)


