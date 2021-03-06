package io.apibuilder.validation

import io.apibuilder.spec.v0.models.Method
import play.api.libs.json._
import org.scalatest.{FunSpec, Matchers}

class MultiServiceSpec2 extends FunSpec with Matchers with helpers.Helpers {

  it("validates unknown operations") {
    flowMultiService.validate("FOO", "/:organization/payments") should equal(
      Left(Seq("HTTP method 'FOO' is invalid. Must be one of: " + Method.all.map(_.toString).mkString(", ")))
    )

    flowMultiService.validate("OPTIONS", "/:organization/payments") should equal(
      Left(Seq("HTTP method 'OPTIONS' not supported for path /:organization/payments - Available methods: GET, POST"))
    )
  }

  it("validates unknown paths") {
    flowMultiService.validate("GET", "/foo") should equal(
      Left(Seq("HTTP path '/foo' is not defined"))
    )
  }

  it("resolves body when path exists in both services") {
    flowMultiService.bodyTypeFromPath("POST", "/:organization/payments") should equal(Some("payment_form"))
  }

  it("resolves body when there are multiple variables in path") {
    flowMultiService.bodyTypeFromPath("POST", "/:organization/shopify/orders/:number/authorizations") should equal(
      Some("io.flow.payment.v0.unions.authorization_form")
    )
  }

  /**
    * This test has two services that define methods like:
    *
    * Service1:
    *   - GET /:organization/payments
    *   - POST /:organization/payments
    *
    * Service2:
    *   - GET /:organization/payments
    *
    * We are testing that we can correctly resolve the POST, fixing a bug where
    * we were propagating the fact that service2 does not define POST through the
    * validation methods (vs. correctly resolving service 1)
    */
  it("validates when path exists in both services with different available methods") {
    flowMultiService.upcast(
      "POST",
      "/:organization/payments",
      Json.obj(
        "discriminator" -> "merchant_of_record_payment_form",
        "method" -> "paypal",
        "order_number" -> "F1001",
        "amount" -> 1.00,
        "currency" -> "CAD"
      )
    ).right.getOrElse {
      sys.error("Failed to validate payment_form")
    }
  }

  it("validateResponseCode") {
    val op = rightOrErrors(
      flowMultiService.validate("POST", "/:organization/payments")
    )

    Seq(201, 401, 422).foreach { code =>
      rightOrErrors {
        flowMultiService.validateResponseCode(op, code)
      }
    }

    Seq(100, 200, 417, 500, 503).foreach { code =>
      flowMultiService.validateResponseCode(op, code) match {
        case Left(error) => error should equal(
          s"Unexpected response code[$code] for operation[POST /:organization/payments]. Declared response codes: 201, 401, 422"
        )
        case Right(v) => sys.error(s"Expected error but got: $v")
      }
    }
  }

  it("response") {
    val op = rightOrErrors(
      flowMultiService.validate("POST", "/:organization/cards")
    )

    flowMultiService.response(op, 201).get.`type` should equal("card")
    flowMultiService.response(op, 499) should be(None)
  }
}
