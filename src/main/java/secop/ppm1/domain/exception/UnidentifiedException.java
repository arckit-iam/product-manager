// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.exception;

public class UnidentifiedException extends RuntimeException {
    public UnidentifiedException(String message){
        super(message);
    }
}