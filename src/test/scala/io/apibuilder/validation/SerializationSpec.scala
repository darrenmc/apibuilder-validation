package io.apibuilder.validation

import io.flow.v0.models.json._

import io.flow.v0.models.{Image, ImageForm, ItemForm}
import org.scalatest.{FunSpec, Matchers}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

class SerializationSpec extends FunSpec with Matchers with helpers.Helpers {

  it("serialize and deserialize item form") {
    val form = ItemForm(
      number = "1",
      locale = "en-US",
      name = "test",
      currency = "USD",
      price = 100,
      categories = Some(Seq("shoes", "dresses")),
      description = Some("desc"),
      attributes = Some(
        Map(
          "coo" -> "CHN",
          "material" -> "leather"
        )
      ),
      images = Some(
        Seq(ImageForm(url = "http://iomage.jpg"))
      )
    )
    val encoded = FormData.toEncoded(Json.toJson(form))
    println(encoded)
    encoded should equal(
      "number=1&name=test&description=desc&price=100.0&attributes[coo]=CHN&attributes[material]=leather&locale=en-US&categories[0]=shoes&categories[1]=dresses&currency=USD&images[0][url]=http%3A%2F%2Fiomage.jpg"
    )

    val deserializedForm = rightOrErrors {
      flowMultiService.upcast(
        "item_form",
        FormData.toJson(
          FormData.parseEncoded(encoded)
        )
      )
    }.as[ItemForm]
    deserializedForm.number should equal(form.number)
    deserializedForm.price should equal(form.price)
    deserializedForm.categories should equal(form.categories)
    deserializedForm.attributes should equal(form.attributes)
    deserializedForm.images should equal(form.images)
  }

}

