/*
 * Copyright 2023-2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.datacapture.views.factories

import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.fhir.datacapture.R
import com.google.android.fhir.datacapture.extensions.displayString
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.NotValidated
import com.google.android.fhir.datacapture.views.QuestionTextConfiguration
import com.google.android.fhir.datacapture.views.QuestionnaireViewItem
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DropDownViewHolderFactoryTest {
  private val parent =
    FrameLayout(
      Robolectric.buildActivity(AppCompatActivity::class.java).create().get().apply {
        setTheme(com.google.android.material.R.style.Theme_Material3_DayNight)
      },
    )
  private val viewHolder = DropDownViewHolderFactory.create(parent)

  @Test
  fun shouldSetQuestionHeader() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { text = "Question?" },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextView>(R.id.question).text.toString())
      .isEqualTo("Question?")
  }

  @Test
  fun shouldPopulateDropDown() {
    val answerOption =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value = Coding().setCode("test-code").setDisplay("Test Code")
      }
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { addAnswerOption(answerOption) },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )
    val selectedItem =
      viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.auto_complete).adapter.getItem(1)
        as DropDownAnswerOption

    assertThat(selectedItem.answerOptionString).isEqualTo("Test Code")
  }

  @Test
  fun `should populate dropdown with display for reference value type`() {
    val answerOption =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value =
          Reference().apply {
            reference = "Patient/123"
            display = "John Doe"
          }
      }
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { addAnswerOption(answerOption) },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )
    val selectedItem =
      viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.auto_complete).adapter.getItem(1)
        as DropDownAnswerOption

    assertThat(selectedItem.answerOptionString).isEqualTo("John Doe")
  }

  @Test
  fun `should populate dropdown with type and id for reference value type if missing display`() {
    val answerOption =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value = Reference().apply { reference = "Patient/123" }
      }
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { addAnswerOption(answerOption) },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )
    val selectedItem =
      viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.auto_complete).adapter.getItem(1)
        as DropDownAnswerOption

    assertThat(selectedItem.answerOptionString).isEqualTo("Patient/123")
  }

  @Test
  fun shouldSetDropDownOptionToCodeIfValueCodingDisplayEmpty() {
    val answerOption =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value = Coding().apply { code = "test-code" }
      }
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { addAnswerOption(answerOption) },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )
    val selectedItem =
      viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.auto_complete).adapter.getItem(1)
        as DropDownAnswerOption
    assertThat(selectedItem.answerOptionString).isEqualTo("test-code")
  }

  @Test
  fun shouldSetAutoTextViewEmptyIfAnswerNull() {
    val answerOption =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value =
          Coding().apply {
            code = "test-code"
            display = "Test Code"
          }
      }
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { addAnswerOption(answerOption) },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    assertThat(
        viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.auto_complete).text.toString(),
      )
      .isEqualTo("")
  }

  @Test
  fun shouldAutoCompleteTextViewToDisplayIfAnswerNotNull() {
    val answerOption =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value =
          Coding().apply {
            code = "test-code"
            display = "Test Code"
          }
      }
    val fakeAnswerValueSetResolver = { uri: String ->
      if (uri == "http://coding-value-set-url") {
        listOf(answerOption)
      } else {
        emptyList()
      }
    }
    val questionnaireItem =
      Questionnaire.QuestionnaireItemComponent().apply {
        answerValueSet = "http://coding-value-set-url"
      }
    viewHolder.bind(
      QuestionnaireViewItem(
        questionnaireItem,
        QuestionnaireResponse.QuestionnaireResponseItemComponent().apply {
          addAnswer(
            QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().apply {
              value = answerOption.value
            },
          )
        },
        enabledAnswerOptions = fakeAnswerValueSetResolver.invoke(questionnaireItem.answerValueSet),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    assertThat(
        viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.auto_complete).text.toString(),
      )
      .isEqualTo(answerOption.value.displayString(parent.context))
  }

  @Test
  fun shouldAutoCompleteTextViewToDisplayIfAnswerNotNullAndDisplayMatchesMoreThanOneOption() {
    val answerOption1 =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value =
          Reference().apply {
            reference = "Patient/1234"
            display = "John"
          }
      }

    val answerOption2 =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value =
          Reference().apply {
            reference = "Patient/6789"
            display = "John"
          }
      }

    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply {
          addAnswerOption(answerOption1)
          addAnswerOption(answerOption2)
        },
        QuestionnaireResponse.QuestionnaireResponseItemComponent().apply {
          addAnswer(
            QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().apply {
              value = answerOption2.value
            },
          )
        },
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    assertThat(
        viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.auto_complete).text.toString(),
      )
      .isEqualTo(answerOption2.value.displayString(parent.context))
  }

  @Test
  fun displayValidationResult_error_shouldShowErrorMessage() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { required = true },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = Invalid(listOf("Missing answer for required field.")),
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextInputLayout>(R.id.text_input_layout).error)
      .isEqualTo("Missing answer for required field.")
  }

  @Test
  fun displayValidationResult_noError_shouldShowNoErrorMessage() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply {
          required = true
          addAnswerOption(
            Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
              value = Coding().apply { display = "display" }
            },
          )
        },
        QuestionnaireResponse.QuestionnaireResponseItemComponent()
          .addAnswer(
            QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().apply {
              value = Coding().apply { display = "display" }
            },
          ),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextInputLayout>(R.id.text_input_layout).error)
      .isNull()
  }

  @Test
  fun `hides error textview in the header`() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent(),
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextView>(R.id.error_text_at_header).visibility)
      .isEqualTo(View.GONE)
  }

  @Test
  fun shouldHideClearIconWhenTextIsEmpty() {
    val answerOption =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value =
          Coding().apply {
            code = "code"
            display = "display"
          }
      }

    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { addAnswerOption(answerOption) },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    val clearIcon = viewHolder.itemView.findViewById<ImageView>(R.id.clear_input_icon)
    assertThat(clearIcon.visibility).isEqualTo(View.GONE)
  }

  @Test
  fun shouldShowClearIconWhenTextIsNotEmpty() {
    val answerOption =
      Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
        value =
          Coding().apply {
            code = "code"
            display = "display"
          }
      }

    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { addAnswerOption(answerOption) },
        QuestionnaireResponse.QuestionnaireResponseItemComponent().apply {
          addAnswer().apply { value = answerOption.valueCoding }
        },
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    val clearIcon = viewHolder.itemView.findViewById<ImageView>(R.id.clear_input_icon)
    assertThat(clearIcon.visibility).isEqualTo(View.VISIBLE)
  }

  @Test
  fun bind_readOnly_shouldDisableView() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { readOnly = true },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextInputLayout>(R.id.text_input_layout).isEnabled)
      .isFalse()
  }

  @Test
  fun `shows asterisk`() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply {
          text = "Question?"
          required = true
        },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
        questionViewTextConfiguration = QuestionTextConfiguration(showAsterisk = true),
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextView>(R.id.question).text.toString())
      .isEqualTo("Question? *")
  }

  @Test
  fun `hide asterisk`() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply {
          text = "Question?"
          required = true
        },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
        questionViewTextConfiguration = QuestionTextConfiguration(showAsterisk = false),
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextView>(R.id.question).text.toString())
      .isEqualTo("Question?")
  }

  @Test
  fun `shows required text`() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { required = true },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
        questionViewTextConfiguration = QuestionTextConfiguration(showRequiredText = true),
      ),
    )

    assertThat(
        viewHolder.itemView
          .findViewById<TextInputLayout>(R.id.text_input_layout)
          .helperText
          .toString(),
      )
      .isEqualTo("Required")
  }

  @Test
  fun `hide required text`() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent().apply { required = true },
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
        questionViewTextConfiguration = QuestionTextConfiguration(showRequiredText = false),
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextInputLayout>(R.id.text_input_layout).helperText)
      .isNull()
  }

  @Test
  fun `shows optional text`() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent(),
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
        questionViewTextConfiguration = QuestionTextConfiguration(showOptionalText = true),
      ),
    )

    assertThat(
        viewHolder.itemView
          .findViewById<TextInputLayout>(R.id.text_input_layout)
          .helperText
          .toString(),
      )
      .isEqualTo("Optional")
  }

  @Test
  fun `hide optional text`() {
    viewHolder.bind(
      QuestionnaireViewItem(
        Questionnaire.QuestionnaireItemComponent(),
        QuestionnaireResponse.QuestionnaireResponseItemComponent(),
        validationResult = NotValidated,
        answersChangedCallback = { _, _, _, _ -> },
        questionViewTextConfiguration = QuestionTextConfiguration(showOptionalText = false),
      ),
    )

    assertThat(viewHolder.itemView.findViewById<TextInputLayout>(R.id.text_input_layout).helperText)
      .isNull()
  }
}
