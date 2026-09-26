/*
 * Copyright (c) 2026 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.s3_v2

import io.airbyte.cdk.ConfigErrorException
import io.airbyte.cdk.command.ValidatedJsonUtils
import io.airbyte.cdk.load.command.s3.S3AddressingStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class S3V2SpecificationTest {

    @Test
    fun `defaults to path-style addressing`() {
        assertEquals(
            S3AddressingStyle.PATH_STYLE,
            parse("{}").toS3BucketConfiguration().s3AddressingStyle
        )
    }

    @Test
    fun `parses explicit path-style addressing`() {
        val specification = parse("""{"s3_addressing_style":"path_style"}""")

        assertEquals(
            S3AddressingStyle.PATH_STYLE,
            specification.toS3BucketConfiguration().s3AddressingStyle
        )
    }

    @Test
    fun `parses virtual-hosted-style addressing`() {
        val specification = parse("""{"s3_addressing_style":"virtual_hosted_style"}""")

        assertEquals(
            S3AddressingStyle.VIRTUAL_HOSTED_STYLE,
            specification.toS3BucketConfiguration().s3AddressingStyle
        )
    }

    @Test
    fun `null addressing style maps to the backward-compatible default`() {
        val specification = parse("""{"s3_addressing_style":null}""")

        assertEquals(
            S3AddressingStyle.PATH_STYLE,
            specification.toS3BucketConfiguration().s3AddressingStyle
        )
    }

    @Test
    fun `rejects unknown addressing styles`() {
        assertThrows(ConfigErrorException::class.java) {
            parse("""{"s3_addressing_style":"unknown"}""")
        }
    }

    @Test
    fun `exposes only the optional snake-case addressing option`() {
        val schema = ValidatedJsonUtils.generateAirbyteJsonSchema(S3V2Specification::class.java)
        val properties = schema.get("properties")
        val addressingStyle = properties.get("s3_addressing_style")

        assertEquals("path_style", addressingStyle.get("default").asText())
        assertEquals(
            listOf("path_style", "virtual_hosted_style"),
            addressingStyle.get("enum").map { it.asText() }
        )
        assertFalse(properties.has("s3AddressingStyle"))
        assertFalse(schema.get("required").any { it.asText() == "s3_addressing_style" })
    }

    private fun parse(json: String): S3V2Specification =
        ValidatedJsonUtils.parseUnvalidated(json, S3V2Specification::class.java)
}
