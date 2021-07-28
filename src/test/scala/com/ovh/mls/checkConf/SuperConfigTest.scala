package com.ovh.mls.checkConf

import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import SuperConfig.superConfig

class SuperConfigTest extends AnyFlatSpec with Matchers {
    val config = ConfigFactory.parseResources("test.conf")

    "generatePathSet" should "only generate full paths" in {
        config.generatePathSet() shouldBe Set("a.b", "a.e.f", "c")
    }

    "generateSubPathSet" should "generate all the paths" in {
        config.generateSubPathSet() shouldBe Set("a.b", "a.e.f", "c", "a", "a.e")
    }
}
