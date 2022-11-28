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

package modelling

import scalismo.statisticalmodel.dataset.DataCollection
import data.DataRepository
import scalismo.geometry._3D
import scalismo.statisticalmodel.PointDistributionModel.apply
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.utils.Random
import scalismo.ui.api.ScalismoUIHeadless
import scalismo.ui.api.ScalismoUI
import scalismo.statisticalmodel.PointDistributionModel
import scalismo.common.interpolation.TriangleMeshInterpolator3D
import scalismo.mesh.TriangleMesh

/**
 * Builds a point distribution model (GP-Model, where the mean and covariance is estimated from the example faces).
 */
object BuildPDM :

    /**
     * Downsamples the model to the given number of points
     */
    def decimateModel(pdm : PointDistributionModel[_3D, TriangleMesh], numberOfPoints : Int) : PointDistributionModel[_3D, TriangleMesh] =
        val decimatedReference = pdm.reference.operations.decimate(numberOfPoints)
        pdm.newReference(decimatedReference, TriangleMeshInterpolator3D())


    def main(args : Array[String]) : Unit = 

        given rng : Random = Random(42)
        scalismo.initialize()

        // use ScalismoUIHeadless instead of ScalismoUI to suppress graphical output
        //val ui = ScalismoUIHeadless() 
        val ui = ScalismoUI()

        val refMesh = DataRepository.loadReferenceMesh().get
        val meshes = for (id <- DataRepository.ids) yield 
            DataRepository.loadFaceMesh(id).get

        val dataCollection = DataCollection.fromTriangleMesh3DSequence(refMesh, meshes)
        val gpaAlignedData = DataCollection.gpa(dataCollection)

        val pdm = PointDistributionModel.createUsingPCA(gpaAlignedData)

        // the meshes we use are really high-res. There is no reason for 
        //  a model to be of such a high resolution. We decimate it.
        val pdmLowRes = decimateModel(pdm, 5000)

        DataRepository.writeFaceModel(pdmLowRes).get
        
        val modelGroup = ui.createGroup("model")
        ui.show(modelGroup, pdmLowRes, "face-model")