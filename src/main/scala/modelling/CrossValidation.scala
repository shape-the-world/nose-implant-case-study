/*
 * Copyright 2015 University of Basel, Department of Mathematics and Computer Science
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

package modelling

import scalismo.utils.Random
import data.DataRepository
import scalismo.statisticalmodel.dataset.DataCollection
import scalismo.statisticalmodel.PointDistributionModel
import scalismo.mesh.MeshMetrics
import scalismo.common.interpolation.NearestNeighborInterpolator1D.apply
import scalismo.common.interpolation.TriangleMeshInterpolator3D
import scalismo.ui.api.{ScalismoUI, ScalismoUIHeadless}
import scalismo.common.PointId
import scalismo.common.DiscreteField.ScalarMeshField
import scalismo.common.ScalarMeshField
import scalismo.mesh.{TriangleMesh}
import scalismo.geometry._3D
import scalismo.ui.model.properties.ScalarRange
import scalismo.io.MeshIO

/**
  * Crossvalidate a model and computes the distance from the best reconstruction. 
  * For visualization purposes, the meshes are colored and written to a temporary directory
  */
object CrossValidation:

    def colorByDist(meshToColor : TriangleMesh[_3D], meshToCompareTo : TriangleMesh[_3D]) : ScalarMeshField[Double] = 
        val distances = for (pt <- meshToColor.pointSet.points) yield meshToCompareTo.operations.closestPointOnSurface(pt).distance
        ScalarMeshField(meshToColor, distances.toIndexedSeq)

    def main(args : Array[String]) : Unit = 

        given rng : Random = Random(42)
        scalismo.initialize()

        
        val ui = ScalismoUIHeadless() // replace with ScalismoUI for visualization of the mesh
        val reconstructionGroup = ui.createGroup("reconstructions")

        val refMesh = DataRepository.loadReferenceMesh().get
        val meshes = for (id <- DataRepository.ids) yield 
            DataRepository.loadFaceMesh(id).get

        val dataCollection = DataCollection.fromTriangleMesh3DSequence(refMesh, meshes)

        val cvFolds = dataCollection.createCrossValidationFolds(dataCollection.size)

        val dists = for ((cvFold, i) <- cvFolds.zipWithIndex) yield 
            val gpaAlignedDc = DataCollection.gpa(cvFold.trainingData)

            val model = BuildPDM.decimateModel(
                PointDistributionModel.createUsingPCA(gpaAlignedDc), 5000
            )

            //use this code instead if you like to cross-validate an augmented model instead
            // val model = AugmentModel.augmentModel(
            //     BuildPDM.decimateModel(
            //         PointDistributionModel.createUsingPCA(gpaAlignedDc),
            //         numberOfPoints = 5000
            //         ), 
            //     )
                
            val df = cvFold.testingData.dataItems.head

            val dfInterpolated = df.interpolate(TriangleMeshInterpolator3D())
            val mesh = model.reference.transform(p => p + dfInterpolated(p))

            val bestModelReconstruction = model.project(mesh)

            // visualization
            val coloredMesh : ScalarMeshField[Double] = colorByDist(bestModelReconstruction, mesh)
            val view = ui.show(reconstructionGroup, coloredMesh, "rec")
            view.scalarRange = ScalarRange(0, 10)
            
            val cvDir = java.io.File("./tmp/cv")
            cvDir.mkdirs()
            MeshIO.writeScalarMeshField(coloredMesh, java.io.File(cvDir, s"mesh-$i.vtk"))

            val dist = MeshMetrics.avgDistance(bestModelReconstruction, mesh)
            println("computed dist " + dist)
            dist


        println("largest average distance between mesh and reconstruction " + dists.max)
        