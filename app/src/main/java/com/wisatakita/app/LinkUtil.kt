package com.wisatakita.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/**
 * LinkUtil — Safe external link launcher.
 *
 * Handles:
 * - Geo URIs: opens with explicit Google Maps preference, falls back to any maps app, falls back to browser
 * - Ticket URLs: opens in browser with chooser
 * - Phone: parses and dials
 * - Share: formats a share text
 */
object LinkUtil {

    private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

    /**
     * Opens a precise location pin using geo: URI.
     * Prefers Google Maps if installed; otherwise shows the system chooser.
     */
    fun openMapPin(activity: android.app.Activity, lat: Double, lng: Double, label: String) {
        // geo:lat,lng?q=lat,lng(label) — standard format for precise pin
        val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")

        // Check if Google Maps is installed
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            `package` = GOOGLE_MAPS_PACKAGE
        }

        val isGoogleMapsInstalled = try {
            activity.packageManager.getPackageInfo(GOOGLE_MAPS_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        if (isGoogleMapsInstalled) {
            try {
                activity.startActivity(googleMapsIntent)
                return
            } catch (_: Exception) {}
        }

        // Fall back to any maps app that handles geo: URIs
        val chooserIntent = Intent.createChooser(
            Intent(Intent.ACTION_VIEW, geoUri),
            activity.getString(R.string.open_in)
        )
        try {
            activity.startActivity(chooserIntent)
        } catch (_: Exception) {
            // Last resort: open Google Maps in browser
            val browserUri = Uri.parse("https://maps.google.com/?q=$lat,$lng")
            activity.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    /**
     * Opens a ticket/external URL safely in browser.
     */
    fun openTicketUrl(activity: android.app.Activity, url: String) {
        if (url.isBlank()) return
        val safeUrl = if (url.startsWith("http")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))
        try {
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.open_in)))
        } catch (_: Exception) {
            Toast.makeText(activity, activity.getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares a destination as formatted text.
     */
    fun shareDestination(activity: android.app.Activity, name: String, location: String, lat: Double, lng: Double) {
        val text = activity.getString(R.string.share_destination, name, location, lat.toString(), lng.toString())
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(intent, "Bagikan $name"))
    }
}
