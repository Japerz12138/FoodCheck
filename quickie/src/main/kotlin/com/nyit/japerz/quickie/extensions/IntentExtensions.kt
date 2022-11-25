package com.nyit.japerz.quickie.extensions

import android.content.Intent
import com.google.mlkit.vision.barcode.common.Barcode
import com.nyit.japerz.quickie.QRScannerActivity
import com.nyit.japerz.quickie.content.AddressParcelable
import com.nyit.japerz.quickie.content.CalendarDateTimeParcelable
import com.nyit.japerz.quickie.content.CalendarEventParcelable
import com.nyit.japerz.quickie.content.ContactInfoParcelable
import com.nyit.japerz.quickie.content.EmailParcelable
import com.nyit.japerz.quickie.content.GeoPointParcelable
import com.nyit.japerz.quickie.content.PersonNameParcelable
import com.nyit.japerz.quickie.content.PhoneParcelable
import com.nyit.japerz.quickie.content.QRContent
import com.nyit.japerz.quickie.content.QRContent.CalendarEvent
import com.nyit.japerz.quickie.content.QRContent.CalendarEvent.CalendarDateTime
import com.nyit.japerz.quickie.content.QRContent.ContactInfo
import com.nyit.japerz.quickie.content.QRContent.ContactInfo.Address
import com.nyit.japerz.quickie.content.QRContent.ContactInfo.Address.AddressType
import com.nyit.japerz.quickie.content.QRContent.ContactInfo.PersonName
import com.nyit.japerz.quickie.content.QRContent.Email
import com.nyit.japerz.quickie.content.QRContent.Email.EmailType
import com.nyit.japerz.quickie.content.QRContent.GeoPoint
import com.nyit.japerz.quickie.content.QRContent.Phone
import com.nyit.japerz.quickie.content.QRContent.Phone.PhoneType
import com.nyit.japerz.quickie.content.QRContent.Plain
import com.nyit.japerz.quickie.content.QRContent.Sms
import com.nyit.japerz.quickie.content.QRContent.Url
import com.nyit.japerz.quickie.content.QRContent.Wifi
import com.nyit.japerz.quickie.content.SmsParcelable
import com.nyit.japerz.quickie.content.UrlBookmarkParcelable
import com.nyit.japerz.quickie.content.WifiParcelable

internal fun Intent?.toQuickieContentType(): QRContent {
  val rawValue = this?.getStringExtra(QRScannerActivity.EXTRA_RESULT_VALUE).orEmpty()
  return this?.toQuickieContentType(rawValue) ?: Plain(rawValue)
}

@Suppress("LongMethod")
private fun Intent.toQuickieContentType(rawValue: String): QRContent? {
  return when (getIntExtra(QRScannerActivity.EXTRA_RESULT_TYPE, Barcode.TYPE_UNKNOWN)) {
    Barcode.TYPE_CONTACT_INFO -> {
      getParcelableExtra<ContactInfoParcelable>(QRScannerActivity.EXTRA_RESULT_PARCELABLE)?.run {
        ContactInfo(
          rawValue = rawValue,
          addresses = addressParcelables.map { it.toAddress() },
          emails = emailParcelables.map { it.toEmail(rawValue) },
          name = nameParcelable.toPersonName(),
          organization = organization,
          phones = phoneParcelables.map { it.toPhone(rawValue) },
          title = title,
          urls = urls
        )
      }
    }
    Barcode.TYPE_EMAIL -> {
      getParcelableExtra<EmailParcelable>(QRScannerActivity.EXTRA_RESULT_PARCELABLE)?.run {
        Email(
          rawValue = rawValue,
          address = address,
          body = body,
          subject = subject,
          type = EmailType.values().getOrElse(type) { EmailType.UNKNOWN }
        )
      }
    }
    Barcode.TYPE_PHONE -> {
      getParcelableExtra<PhoneParcelable>(QRScannerActivity.EXTRA_RESULT_PARCELABLE)?.run {
        Phone(rawValue = rawValue, number = number, type = PhoneType.values().getOrElse(type) { PhoneType.UNKNOWN })
      }
    }
    Barcode.TYPE_SMS -> {
      getParcelableExtra<SmsParcelable>(QRScannerActivity.EXTRA_RESULT_PARCELABLE)?.run {
        Sms(rawValue = rawValue, message = message, phoneNumber = phoneNumber)
      }
    }
    Barcode.TYPE_URL -> {
      getParcelableExtra<UrlBookmarkParcelable>(QRScannerActivity.EXTRA_RESULT_PARCELABLE)?.run {
        Url(rawValue = rawValue, title = title, url = url)
      }
    }
    Barcode.TYPE_WIFI -> {
      getParcelableExtra<WifiParcelable>(QRScannerActivity.EXTRA_RESULT_PARCELABLE)?.run {
        Wifi(rawValue = rawValue, encryptionType = encryptionType, password = password, ssid = ssid)
      }
    }
    Barcode.TYPE_GEO -> {
      getParcelableExtra<GeoPointParcelable>(QRScannerActivity.EXTRA_RESULT_PARCELABLE)?.run {
        GeoPoint(rawValue = rawValue, lat = lat, lng = lng)
      }
    }
    Barcode.TYPE_CALENDAR_EVENT -> {
      getParcelableExtra<CalendarEventParcelable>(QRScannerActivity.EXTRA_RESULT_PARCELABLE)?.run {
        CalendarEvent(
          rawValue = rawValue,
          description = description,
          end = end.toCalendarEvent(),
          location = location,
          organizer = organizer,
          start = start.toCalendarEvent(),
          status = status,
          summary = summary
        )
      }
    }
    else -> null
  }
}

internal fun Intent?.getRootException(): Exception {
  this?.getSerializableExtra(QRScannerActivity.EXTRA_RESULT_EXCEPTION).let {
    return if (it is Exception) it else IllegalStateException("Could retrieve root exception")
  }
}

private fun PhoneParcelable.toPhone(rawValue: String) =
  Phone(rawValue = rawValue, number = number, type = PhoneType.values().getOrElse(type) { PhoneType.UNKNOWN })

private fun EmailParcelable.toEmail(rawValue: String) =
  Email(
    rawValue = rawValue,
    address = address,
    body = body,
    subject = subject,
    type = EmailType.values().getOrElse(type) { EmailType.UNKNOWN }
  )

private fun AddressParcelable.toAddress() =
  Address(addressLines = addressLines, type = AddressType.values().getOrElse(type) { AddressType.UNKNOWN })

private fun PersonNameParcelable.toPersonName() =
  PersonName(
    first = first,
    formattedName = formattedName,
    last = last,
    middle = middle,
    prefix = prefix,
    pronunciation = pronunciation,
    suffix = suffix
  )

private fun CalendarDateTimeParcelable.toCalendarEvent() =
  CalendarDateTime(
    day = day,
    hours = hours,
    minutes = minutes,
    month = month,
    seconds = seconds,
    year = year,
    utc = utc
  )