## Problem Analysis
The error occurs when deleting a carousel (banner) because the frontend is incorrectly calling the `deleteBanner` API function.

**Root Cause:**
1. The backend `BannerController.deleteBanner()` expects a `Long` type for the `id` parameter
2. The frontend `BannerManagePage.handleDelete()` passes the ID directly as a string to `deleteBanner()`
3. The `deleteBanner` API function expects an object with an `id` property (`API.deleteBannerParams`)
4. When called with a direct string, destructuring fails and `param0` becomes `undefined`
5. This results in the URL `/banner/delete/undefined`, causing Spring Boot to throw a `NumberFormatException` when converting 'undefined' to Long

## Fix Plan
1. **Modify `BannerManagePage.vue`**: Update the `handleDelete` function to call `deleteBanner` with the correct parameter format
2. **Change from**: `await deleteBanner(id)`
3. **Change to**: `await deleteBanner({ id })`

## Files to Modify
- `clothingPattern-front/src/pages/admin/BannerManagePage.vue`

## Expected Result
After the fix, when deleting a carousel, the ID will be correctly passed to the backend API, avoiding the 'undefined' parameter issue and resolving the deletion failure.