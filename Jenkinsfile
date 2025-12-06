/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 ********************************************************************************/
 
pipeline {
	agent {
		label 'centos-7'
	}

	tools {
		jdk 'temurin-jdk17-latest'
	}

	stages {
		stage ('Build') {
			steps {
				sh './gradlew clean build -x buildOpenmdmImage -x integrationTest'
			}
		}
	}

	post {
		always {
			junit '**/build/test-results/**/*.xml'
			archiveArtifacts artifacts: '**/build/distributions/*.zip', fingerprint: true
		}
	}
}