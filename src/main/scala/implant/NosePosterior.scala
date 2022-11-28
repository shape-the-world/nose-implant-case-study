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
package implant

import data.DataRepository
import scalismo.ui.api.ScalismoUI
import scalismo.mesh.TriangleMesh
import scalismo.geometry._3D
import scalismo.statisticalmodel.PointDistributionModel
import scalismo.numerics.UniformMeshSampler3D

import scalismo.utils.Random
import scalismo.kernels.GaussianKernel3D
import scalismo.statisticalmodel.GaussianProcess
import scalismo.kernels.DiagonalKernel3D
import scalismo.geometry.EuclideanVector
import scalismo.statisticalmodel.LowRankGaussianProcess
import data.DataRepository
import scalismo.common.interpolation.TriangleMeshInterpolator3D
import scalismo.statisticalmodel.PointDistributionModel
import scalismo.ui.api.ScalismoUI
import scalismo.mesh.TriangleMesh
import scalismo.io.StatisticalModelIO

/**
  * Given a mesh without a nose (or any other missing part), this script
  * computes a posterior model where the given part remains fixed and the missing part is 
  * filled in by the model.
  */
object NosePosterior:
  
    /**
      * Computes the posterior model iteratively. As we cannot trust that in the first iteration 
      * the closest points to the mean are good correspondences, we compute the posterior several times 
      * using an icp scheme
      */
    def icpPosterior(pdm : PointDistributionModel[_3D, TriangleMesh],  target : TriangleMesh[_3D], numIterations : Int)
        (using rng : scalismo.utils.Random) : PointDistributionModel[_3D, TriangleMesh] = 

        if numIterations == 0 then 
            pdm
        else 
            val targetPoints = UniformMeshSampler3D(target, 1000).sample().map(_._1)
            val currentInstance = pdm.mean
            val idsOfclosestPointOnRef = targetPoints.map(targetPoint => currentInstance.pointSet.findClosestPoint(targetPoint).id)

            val regressionData = idsOfclosestPointOnRef.zip(targetPoints)
            icpPosterior(pdm.posterior(regressionData, sigma2 = 1.0), target, numIterations - 1)


    /**
      * Augments the model with smooth deformation to make it a bit more flexible before computing the posterior.
      */
    def augmentModel(pdm : PointDistributionModel[_3D, TriangleMesh]) : PointDistributionModel[_3D, TriangleMesh] = 
        val biasCov = DiagonalKernel3D(GaussianKernel3D(50.0, scaleFactor = 1), outputDim = 3)
        val biasModel = GaussianProcess[_3D, EuclideanVector[_3D]](biasCov)
        val lowRankBias = LowRankGaussianProcess.approximateGPCholesky(pdm.reference, biasModel, 1e-2, TriangleMeshInterpolator3D())
        PointDistributionModel.augmentModel(pdm, lowRankBias)


    def main(args : Array[String]) : Unit =

        scalismo.initialize()
        given rng : scalismo.utils.Random = scalismo.utils.Random(32)

        val ui = ScalismoUI()

        val pdm = augmentModel(DataRepository.loadAugmentedFaceModel().get.truncate(100))
        val modelGroup = ui.createGroup("pdm")
        val pdmView = ui.show(modelGroup, pdm, "pdm")

        val targetMesh = DataRepository.loadFaceMeshNoNose().get.operations.decimate(3000)
        val targetGroup = ui.createGroup("target")
        
        ui.show(targetGroup, targetMesh, "target")

        val posteriorpdm = icpPosterior(pdm, targetMesh, 5)
        val rec = posteriorpdm.mean
        
        val posteriorGroup = ui.createGroup("posterior")
        ui.show(posteriorGroup, posteriorpdm, "posterior")
        ui.close()
        StatisticalModelIO.writeStatisticalTriangleMeshModel3D(posteriorpdm, java.io.File("./tmp/nose-model.h5")).get


        

        
